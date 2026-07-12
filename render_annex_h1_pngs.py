"""
Render Annex H-1 (CS Form 212 Revised 2025) PDF pages 1-4 into 612x792 PNG
backgrounds for the PDS Jasper reports.

Source of truth: ANNEX H-1 - CS Form No. 212 Revised 2025 - Personal Data Sheet.pdf
Outputs:         src/main/resources/static/images/PDS2025_P{1,2,3,4}.png

Each output PNG is exactly 612x792 px so the pixel coordinates equal PDF points,
which is what every <reportElement x="..." y="..."/> in the JRXML assumes.

Process:
  1. Render each page at 300 DPI via PyMuPDF (zoom = 300/72 = 4.1667).
  2. Resample to 612x792 with LANCZOS for clean anti-aliased gridlines.
  3. Save as RGB PNG (no alpha) so JasperReports embeds it cleanly.
"""

from pathlib import Path
import fitz
from PIL import Image

ROOT = Path(__file__).resolve().parent
PDF = ROOT / "ANNEX H-1 - CS Form No. 212 Revised 2025 - Personal Data Sheet.pdf"
OUT_DIR = ROOT / "src" / "main" / "resources" / "static" / "images"
TARGET_W, TARGET_H = 612, 792
ZOOM = 300 / 72  # 300 DPI

doc = fitz.open(PDF)
for i in range(doc.page_count):
    page = doc[i]
    matrix = fitz.Matrix(ZOOM, ZOOM)
    pix = page.get_pixmap(matrix=matrix, alpha=False)
    img = Image.frombytes("RGB", (pix.width, pix.height), pix.samples)
    img = img.resize((TARGET_W, TARGET_H), Image.LANCZOS)
    out_path = OUT_DIR / f"PDS2025_P{i+1}.png"
    img.save(out_path, "PNG", optimize=True)
    print(f"wrote {out_path}  ({img.size[0]}x{img.size[1]})")

doc.close()
