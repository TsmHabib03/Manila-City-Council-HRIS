"""
Snap PDS2025_P{2,3}.jrxml grid to the new Annex H-1 '-final (main)' PDF (CR-005).

Both the current template and the new PDF, scaled to the 612x936 long-bond canvas,
already share the same grid within ~1-2pt. This script removes that residual drift by
building a piecewise-linear coordinate map (current grid line -> nearest new-PDF grid
line) and warping EVERY <reportElement> (rectangles, staticText, textField) through it,
so gridlines, labels and data cells all stay in sync and land exactly on the document's
lines.

Run:  python warp_pds_to_newpdf.py
Idempotent-ish: re-running re-snaps to the same targets (deltas converge to 0).
"""
from pathlib import Path
import re, pdfplumber

ROOT = Path(__file__).resolve().parent
PDF = ROOT / "ANNEX H-1 - CS Form No. 212 Revised 2025 - Personal Data Sheet-final (main).pdf"
W, H = 612, 936
JR = lambda n: ROOT / "src" / "main" / "resources" / "jasper" / "reports" / f"PDS2025_P{n}.jrxml"

# ---- extract new-PDF grid lines (scaled to canvas) ----
def pdf_grid(page_index):
    with pdfplumber.open(PDF) as pdf:
        page = pdf.pages[page_index]
        sy, sx = H/page.height, W/page.width
        hs, vs = set(), set()
        for r in page.rects:
            x0,x1,top,bot = r['x0'],r['x1'],r['top'],r['bottom']
            if (bot-top)<=2 and (x1-x0)>=50: hs.add(top*sy)
            if (x1-x0)<=2 and (bot-top)>=30: vs.add(x0*sx)
            if (x1-x0)>=50 and (bot-top)>=30:
                hs.update([top*sy, bot*sy]); vs.update([x0*sx, x1*sx])
        for ln in page.lines:
            x0,x1,top,bot = ln['x0'],ln['x1'],ln['top'],ln['bottom']
            if abs(bot-top)<=2 and abs(x1-x0)>=50: hs.add(top*sy)
            if abs(x1-x0)<=2 and abs(bot-top)>=30: vs.add(x0*sx)
    return _cluster(sorted(hs)), _cluster(sorted(vs))

def _cluster(vals, tol=2.0):
    """Merge values within tol into their mean."""
    out=[]; group=[]
    for v in vals:
        if group and v-group[-1] > tol:
            out.append(sum(group)/len(group)); group=[]
        group.append(v)
    if group: out.append(sum(group)/len(group))
    return out

# ---- current template grid lines from JRXML rectangles ----
def jrxml_lines(src):
    rects = re.findall(r'<reportElement[^>]*?x="(-?\d+)"\s+y="(-?\d+)"\s+width="(\d+)"\s+height="(\d+)"', src)
    hs=set(); vs=set()
    for x,y,w,h in rects:
        x,y,w,h=int(x),int(y),int(w),int(h)
        if h<=2 and w>=50: hs.add(y)
        if w<=2 and h>=30: vs.add(x)
    return _cluster(sorted(hs)), _cluster(sorted(vs))

# ---- normalize new-PDF lines into the current template's form bounding box ----
# The new PDF's 4 pages are embedded at different scales/margins, so absolute
# scaling distorts width. Instead map the new form's [min..max] onto the current
# form's [min..max] per axis, so the form stays full-width and we adopt only the
# new PDF's *internal* proportions.
def normalize(new, cur):
    n0,n1 = new[0], new[-1]
    c0,c1 = cur[0], cur[-1]
    span_n = (n1-n0) or 1.0
    return [c0 + (v-n0)/span_n*(c1-c0) for v in new]

# ---- build anchor pairs (current -> nearest normalized new pdf line within tol) ----
def anchors(cur, new, tol=6.0):
    new = normalize(new, cur)
    pairs=[]
    for c in cur:
        best=min(new, key=lambda n: abs(n-c))
        if abs(best-c)<=tol:
            pairs.append((c, best))
    pairs=sorted(set(pairs))
    mono=[]
    for c,t in pairs:
        if mono and t<=mono[-1][1]: t=mono[-1][1]+0.01
        mono.append((c,t))
    # always anchor the exact endpoints so the outer form box never moves
    if mono and mono[0][0] != cur[0]:
        mono.insert(0,(cur[0],cur[0]))
    if mono and mono[-1][0] != cur[-1]:
        mono.append((cur[-1],cur[-1]))
    return mono

def make_map(pairs):
    cs=[c for c,_ in pairs]; ts=[t for _,t in pairs]
    def f(v):
        if v<=cs[0]:  # extrapolate from first segment
            if len(cs)>=2:
                slope=(ts[1]-ts[0])/(cs[1]-cs[0]); return ts[0]+slope*(v-cs[0])
            return v+(ts[0]-cs[0])
        if v>=cs[-1]:
            if len(cs)>=2:
                slope=(ts[-1]-ts[-2])/(cs[-1]-cs[-2]); return ts[-1]+slope*(v-cs[-1])
            return v+(ts[-1]-cs[-1])
        for i in range(len(cs)-1):
            if cs[i]<=v<=cs[i+1]:
                slope=(ts[i+1]-ts[i])/(cs[i+1]-cs[i]); return ts[i]+slope*(v-cs[i])
        return v
    return f

def warp(n):
    path=JR(n); src=path.read_text(encoding="utf-8")
    ph,pv = pdf_grid(n-1)
    jh,jv = jrxml_lines(src)
    ymap=make_map(anchors(jh,ph)); xmap=make_map(anchors(jv,pv))

    def repl(m):
        x,y,w,h=int(m.group('x')),int(m.group('y')),int(m.group('w')),int(m.group('h'))
        nx=round(xmap(x)); ny=round(ymap(y))
        nx2=round(xmap(x+w)); ny2=round(ymap(y+h))
        # never collapse a line/box that had positive extent
        nw = max(1, nx2-nx) if w>0 else (nx2-nx)
        nh = max(1, ny2-ny) if h>0 else (ny2-ny)
        return f'{m.group("pre")}x="{nx}" y="{ny}" width="{nw}" height="{nh}"'

    pat=re.compile(r'(?P<pre><reportElement[^>]*?\s)x="(?P<x>-?\d+)"\s+y="(?P<y>-?\d+)"\s+width="(?P<w>\d+)"\s+height="(?P<h>\d+)"')
    new, cnt = pat.subn(repl, src)
    path.write_text(new, encoding="utf-8")
    # report max delta to confirm small moves
    return cnt

if __name__=="__main__":
    for n in (2,3):
        c=warp(n)
        print(f"P{n}: warped {c} reportElements to new-PDF grid")
