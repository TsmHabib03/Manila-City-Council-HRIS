import re

SCALE = 936 / 792   # 1.18181818...

files = [
    "src/main/resources/jasper/reports/PDS2025_P1.jrxml",
    "src/main/resources/jasper/reports/PDS2025_P2.jrxml",
    "src/main/resources/jasper/reports/PDS2025_P3.jrxml",
    "src/main/resources/jasper/reports/PDS2025_P4.jrxml",
]

for path in files:
    text = open(path, encoding="utf-8").read()

    def scale_report_element(m):
        tag = m.group(0)
        tag = re.sub(r'\by="(\d+)"', lambda x: f'y="{round(int(x.group(1)) * SCALE)}"', tag)
        tag = re.sub(r'\bheight="(\d+)"', lambda x: f'height="{round(int(x.group(1)) * SCALE)}"', tag)
        return tag

    text = re.sub(r'<reportElement\b[^/]*/>', scale_report_element, text, flags=re.DOTALL)
    text = text.replace('<band height="792" splitType="Prevent">', '<band height="936" splitType="Prevent">')

    open(path, "w", encoding="utf-8").write(text)
    print(f"Done: {path}")
