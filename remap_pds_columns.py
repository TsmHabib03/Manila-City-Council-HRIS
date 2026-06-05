"""
Remap PDS2025 P2/P3 textField x/width to Annex H-1 column edges.

Source-of-truth column geometry was extracted from drawn rectangles in
ANNEX H-1 - CS Form No. 212 Revised 2025 - Personal Data Sheet.pdf
(see render_annex_h1_pngs.py for the PDF→PNG pipeline that backs these
coordinates).

The previous JRXMLs were calibrated against a different background image,
so almost every dynamic column extends past or sits beside its printed
cell on the new (correct) Annex H-1 background. This script reads each
parameter's prefix to look up the canonical (x, w, font, align, overflow)
and rewrites the matching <reportElement>.

Y-coordinates are left alone — those were already close to correct for
the new background (verified via debug_overlay_pds.py).
"""

from pathlib import Path
import re

ROOT = Path(__file__).resolve().parent
P2 = ROOT / "src" / "main" / "resources" / "jasper" / "reports" / "PDS2025_P2.jrxml"
P3 = ROOT / "src" / "main" / "resources" / "jasper" / "reports" / "PDS2025_P3.jrxml"

# Canonical column geometry per parameter prefix.
#   (x, w, font_size, text_align, overflow, off_page)
# off_page=True positions the cell at x=-200 w=1 (invisible — for legacy
# 2017-form fields like V.WE_Montly_Salary that don't appear in the 2025 form).
P2_COLS = {
    # IV. Civil Service Eligibility (Section 27)
    # Fixed coordinates based on drawn rectangle grid (x=100 left border, x=251 first divider)
    "IV.CSE_Career_Service_RA_1080":   (102,  147, 6, "Left",   True,  False),
    "IV.CSE_Rating":                   (252,  48, 7, "Center", False, False),
    "IV.CSE_Date_Of_Examination":      (303,  53, 6, "Center", False, False),
    "IV.CSE_Place_Of_Examination":     (358,  70, 6, "Left",   True,  False),
    "IV.CSE_License_Number_":          (431,  43, 6, "Center", False, False),
    "IV.CSE_License_Date_Of_Validity": (477,  43, 6, "Center", False, False),
    # V. Work Experience (Section 28)
    "V.WE_Inclusive_Dates_From":            (101, 39, 7, "Center", False, False),
    "V.WE_Inclusive_Dates_To":              (140, 39, 7, "Center", False, False),
    "V.WE_Position_Title":                  (179, 123, 6, "Left", True, False),
    "V.WE_Department_Agency_Office_Company": (302, 127, 6, "Left", True, False),
    "V.WE_Montly_Salary":                   (0, 0, 6, "Left", False, True),
    "V.WE_Salary_Job_PayGrade":             (0, 0, 6, "Left", False, True),
    "V.WE_Status_Of_Appointment":           (429, 46, 7, "Center", False, False),
    "V.WE_Gov_Service":                     (475, 47, 7, "Center", False, False),
    # signature/date footer — leave geometry alone if present
}

P3_COLS = {
    # VI. Voluntary Work (Section 29)
    "VI.VW_Name_Address_Of_Org":   (63, 215, 6, "Left",   True,  False),
    "VI.VW_Inclusive_Dates_From":  (278, 38, 7, "Center", False, False),
    "VI.VW_Inclusive_Dates_To":    (316, 38, 7, "Center", False, False),
    "VI.VW_Number_Of_Hours":       (354, 38, 7, "Center", False, False),
    "VI.VW_Position_Nature_Of_Work": (392, 168, 6, "Left", True, False),
    # VII. Learning & Development (Section 30)
    "VII.LAD_Training_Programs":        (63, 215, 6, "Left",   True,  False),
    "VII.LAD_Inclusive_Dates_Of_Attendance_From": (278, 38, 7, "Center", False, False),
    "VII.LAD_Inclusive_Dates_Of_Attendance_To":   (316, 38, 7, "Center", False, False),
    "VII.LAD_Number_Of_Hours":          (354, 38, 7, "Center", False, False),
    "VII.LAD_Type_Of_LD":               (392, 43, 6, "Center", False, False),
    "VII.Conducted_Sponsored_By":       (435, 125, 6, "Left", True, False),
    # VIII. Other Information (Sections 31-33)
    "VIII.OI_Special_Skills_Hobbies":          (63, 125, 6, "Left", True, False),
    "VIII.OI_NonAcademic_Distinctions_Recognitions": (188, 247, 6, "Left", True, False),
    "VIII.OI_Membership_In_Association":       (435, 125, 6, "Left", True, False),
}

# P4 has no x/width corrections needed, but requires y-coordinate shifts for
# "If YES" detail fields to land below their static "If YES, give details:" labels.
# Each entry: param_name -> new_y (label_y + label_height + 1 or label_y + 10 for 10-high labels)
P4_Y_SHIFTS = {
    "VIII.OI_34_If_Yes":      97,   # label at y=85, h=10 → 85+10+2
    "VIII.OI_35_A_If_Yes":    134,  # label at y=123, h=10 → 123+10+1
    "VIII.OI_35_B_If_Yes":    174,  # label at y=164, h=10 → 164+10
    "VIII.OI_35_Date_Filed":  176,  # label at y=164, h=10 → shift to below label
    "VIII.OI_35_Status_Of_Cases": 187,  # After Date_Filed field
    "VIII.OI_36_If_Yes":      225,  # label at y=214, h=10 → 214+10+1
    "VIII.OI_37_A_If_Yes":    263,  # label at y=252, h=10 → 252+10+1
    "VIII.OI_38_A_If_Yes":    298,  # label at y=287, h=10 → 287+10+1
    "VIII.OI_38_B_If_Yes":    324,  # label at y=313, h=10 → 313+10+1
    "VIII.OI_39_A_If_Yes":    352,  # label at y=341, h=10 → 341+10+1
    "VIII.OI_40_A_If_Yes":    418,  # label at y=407, h=10 → 407+10+1
    "VIII.OI_40_B_If_Yes":    440,  # label at y=429, h=10 → 429+10+1
    "VIII.OI_40_C_If_Yes":    462,  # label at y=451, h=10 → 451+10+1
}


def matches_prefix(param: str, prefixes) -> str | None:
    """Return the matching prefix (longest first) or None."""
    for p in sorted(prefixes, key=len, reverse=True):
        if param.startswith(p):
            return p
    return None


def remap(jrxml_path: Path, cols: dict, y_shifts: dict = None) -> int:
    src = jrxml_path.read_text(encoding="utf-8")
    out = []
    cursor = 0
    changed = 0

    tf_pat = re.compile(r"<textField\b.*?</textField>", re.DOTALL)

    for m in tf_pat.finditer(src):
        out.append(src[cursor : m.start()])
        block = m.group(0)
        pm = re.search(r"\$P\{([^}]+)\}", block)
        if not pm:
            out.append(block)
            cursor = m.end()
            continue
        param = pm.group(1)
        prefix = matches_prefix(param, cols.keys())
        if prefix is None:
            # No x/w change, but check if y-shift applies (for P4)
            if y_shifts and param in y_shifts:
                new_y = y_shifts[param]
                new = re.sub(
                    r'y="\d+"',
                    f'y="{new_y}"',
                    block,
                    count=1,
                )
                if new != block:
                    changed += 1
                out.append(new)
                cursor = m.end()
                continue
            out.append(block)
            cursor = m.end()
            continue

        x, w, font_size, align, overflow, off_page = cols[prefix]
        if off_page:
            x, w = -200, 1

        new = block
        # Update reportElement x and width
        new = re.sub(
            r'(<reportElement\s+)x="-?\d+"(\s+y="-?\d+"\s+)width="\d+"',
            rf'\1x="{x}"\2width="{w}"',
            new,
            count=1,
        )
        # Update isStretchWithOverflow
        overflow_str = "true" if overflow else "false"
        new = re.sub(
            r'isStretchWithOverflow="(?:true|false)"',
            f'isStretchWithOverflow="{overflow_str}"',
            new,
            count=1,
        )
        # Update font size
        new = re.sub(
            r'(<font\s+fontName="[^"]+"\s+size=")\d+(")',
            rf'\g<1>{font_size}\g<2>',
            new,
            count=1,
        )
        # Update textAlignment (keep verticalAlignment Middle)
        new = re.sub(
            r'(<textElement\s+)textAlignment="[^"]+"',
            rf'\1textAlignment="{align}"',
            new,
            count=1,
        )

        if new != block:
            changed += 1
        out.append(new)
        cursor = m.end()
    out.append(src[cursor:])

    jrxml_path.write_text("".join(out), encoding="utf-8")
    return changed


if __name__ == "__main__":
    n2 = remap(P2, P2_COLS)
    n3 = remap(P3, P3_COLS)
    # P4 needs y-shifts for "If YES" detail fields to land below static labels
    p4_path = ROOT / "src" / "main" / "resources" / "jasper" / "reports" / "PDS2025_P4.jrxml"
    n4 = remap(p4_path, {}, P4_Y_SHIFTS)  # empty cols dict (no x/w changes), only y-shifts
    print(f"P2: {n2} textFields remapped")
    print(f"P3: {n3} textFields remapped")
    print(f"P4: {n4} textFields remapped (y-shifts)")
