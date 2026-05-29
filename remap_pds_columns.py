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
    "IV.CSE_Career_Service_RA_1080":   (38,  211, 6, "Left",   True,  False),
    "IV.CSE_Rating":                   (249,  50, 7, "Center", False, False),
    "IV.CSE_Date_Of_Examination":      (300,  50, 6, "Center", False, False),
    "IV.CSE_Place_Of_Examination":     (351,  90, 6, "Left",   True,  False),
    "IV.CSE_License_Number_":          (442,  42, 6, "Center", False, False),
    "IV.CSE_License_Date_Of_Validity": (484,  36, 6, "Center", False, False),
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


def matches_prefix(param: str, prefixes) -> str | None:
    """Return the matching prefix (longest first) or None."""
    for p in sorted(prefixes, key=len, reverse=True):
        if param.startswith(p):
            return p
    return None


def remap(jrxml_path: Path, cols: dict) -> int:
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
    print(f"P2: {n2} textFields remapped")
    print(f"P3: {n3} textFields remapped")
