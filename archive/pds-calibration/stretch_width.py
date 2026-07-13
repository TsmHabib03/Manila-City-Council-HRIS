import re, glob

# Horizontal stretch: widen the vector-drawn PDS form to fill the page width.
# Form left frame sits at x~62; map it to a thin left margin and scale content out.
L = 62      # form left frame anchor
M = 18      # target left margin
S = 1.14    # scale factor (keeps rightmost content < pageWidth 612)

files = sorted(glob.glob('src/main/resources/jasper/reports/PDS2025_P*.jrxml'))

def transform_reportelement(tag):
    def newx(m):
        x = int(m.group(1))
        nx = round((x - L) * S + M)
        if nx < 6: nx = 6
        return f'x="{nx}"'
    def neww(m):
        w = int(m.group(1))
        return f'width="{round(w * S)}"'
    tag = re.sub(r'\bx="(\d+)"', newx, tag, count=1)
    tag = re.sub(r'\bwidth="(\d+)"', neww, tag, count=1)
    return tag

for f in files:
    t = open(f, encoding='utf-8').read()
    t2 = re.sub(r'<reportElement\b[^>]*/>', lambda m: transform_reportelement(m.group(0)), t)
    open(f, 'w', encoding='utf-8').write(t2)
    # report new bounds
    mn=10**9; mx=0
    for m in re.finditer(r'<reportElement[^>]*?x="(\d+)"[^>]*?width="(\d+)"', t2):
        x=int(m.group(1)); w=int(m.group(2)); mn=min(mn,x); mx=max(mx,x+w)
    print(f"{f.split(chr(92))[-1]}: new minX={mn} maxRight={mx} (pageWidth 612)")
print("DONE width stretch")
