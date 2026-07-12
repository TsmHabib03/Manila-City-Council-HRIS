"""
Overlay JRXML textField rectangles on top of PDS2025_P{N}.png so we can visually
verify (and correct) where each field will appear in the rendered PDF.

Usage:  python debug_overlay_pds.py {1|2|3|4}
Output: debug_overlay_p{N}.png in repo root.
"""

import re
import sys
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont

if len(sys.argv) < 2 or sys.argv[1] not in {"1", "2", "3", "4"}:
    print("Usage: python debug_overlay_pds.py {1|2|3|4}")
    sys.exit(1)

page = sys.argv[1]
PNG_PATH = Path(f"src/main/resources/static/images/PDS2025_P{page}.png")
JRXML_PATH = Path(f"src/main/resources/jasper/reports/PDS2025_P{page}.jrxml")
OUT = Path(f"debug_overlay_p{page}.png")

PT_W, PT_H = 612, 792

im = Image.open(PNG_PATH).convert("RGBA").resize((PT_W, PT_H), Image.LANCZOS)
overlay = Image.new("RGBA", im.size, (0, 0, 0, 0))
draw = ImageDraw.Draw(overlay)

try:
    font = ImageFont.truetype("arial.ttf", 7)
except Exception:
    font = ImageFont.load_default()

src = JRXML_PATH.read_text(encoding="utf-8")

# Match every <textField> ... <textFieldExpression>$P{name}</textFieldExpression>
pat = re.compile(
    r'<textField[^>]*>\s*<reportElement\s+x="(-?\d+)"\s+y="(-?\d+)"\s+width="(\d+)"\s+height="(\d+)"[^/]*/>'
    r'.*?<textFieldExpression><!\[CDATA\[\$P\{([^}]+)\}\]\]></textFieldExpression>',
    re.DOTALL,
)

count = 0
for m in pat.finditer(src):
    x, y, w, h, param = int(m[1]), int(m[2]), int(m[3]), int(m[4]), m[5]
    short = param.split(".")[-1].replace("_NO.", "_NO").rstrip(".")
    draw.rectangle([x, y, x + w, y + h], outline=(255, 0, 0, 220), fill=(255, 0, 0, 50), width=1)
    draw.text((x + 1, y - 7), short[:20], fill=(0, 0, 220, 255), font=font)
    count += 1

result = Image.alpha_composite(im, overlay)
result.convert("RGB").save(OUT)
print(f"Drew {count} textField boxes from {JRXML_PATH} -> {OUT}")
