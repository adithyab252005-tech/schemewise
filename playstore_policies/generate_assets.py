from PIL import Image, ImageDraw, ImageFont
import numpy as np

ICON_SRC = r"d:\schemewise_2\android\app\src\main\res\mipmap-xxxhdpi\ic_launcher.png"

# ─── APP ICON 512×512 ────────────────────────────────────────────────────────
icon_raw = Image.open(ICON_SRC).convert("RGBA")

# Scale 192→512 with LANCZOS, then sharpen slightly via a 2x intermediate
icon_2x = icon_raw.resize((384, 384), Image.LANCZOS)
icon_512 = icon_2x.resize((512, 512), Image.LANCZOS)
icon_512.save(r"d:\schemewise_2\playstore_policies\app_icon.png", "PNG", optimize=True)
print("app_icon.png saved (512×512)")

# ─── FEATURE GRAPHIC 1024×500 ────────────────────────────────────────────────
W, H = 1024, 500

# Navy-to-midnight gradient background (left → right, slightly lighter on right)
arr = np.zeros((H, W, 3), dtype=np.uint8)
for x in range(W):
    t = x / (W - 1)
    arr[:, x, 0] = int(6  + 14 * t)          # R  6→20
    arr[:, x, 1] = int(15 + 16 * t)          # G  15→31
    arr[:, x, 2] = int(30 + 50 * t)          # B  30→80

img = Image.fromarray(arr, "RGB")
draw = ImageDraw.Draw(img, "RGBA")

# ── Tricolor accent strip at top ─────────────────────────────────────────────
thirds = W // 3
draw.rectangle([0,        0, thirds,   5], fill="#F97316")
draw.rectangle([thirds,   0, 2*thirds, 5], fill=(255, 255, 255, 60))
draw.rectangle([2*thirds, 0, W,        5], fill="#22C55E")

# ── Subtle vertical divider ───────────────────────────────────────────────────
DIV_X = 590
for y in range(H):
    alpha = 0
    if y > 50:  alpha  = min(y - 50, 30) / 30
    if y < H-50: alpha = min(alpha, min(H - 50 - y, 30) / 30)
    a = int(40 * alpha)
    draw.point((DIV_X, y), fill=(255, 255, 255, a))

# ── Ambient glow orbs ────────────────────────────────────────────────────────
orb = Image.new("RGBA", (500, 500), (0, 0, 0, 0))
od = ImageDraw.Draw(orb)
for r in range(250, 0, -1):
    a = int(35 * (1 - r / 250) ** 2)
    od.ellipse([250-r, 250-r, 250+r, 250+r], fill=(249, 115, 22, a))
img.paste(orb, (-200, -200), orb)   # top-left orange glow

orb2 = Image.new("RGBA", (400, 400), (0, 0, 0, 0))
od2 = ImageDraw.Draw(orb2)
for r in range(200, 0, -1):
    a = int(25 * (1 - r / 200) ** 2)
    od2.ellipse([200-r, 200-r, 200+r, 200+r], fill=(139, 92, 246, a))
img.paste(orb2, (W - 150, H - 150), orb2)  # bottom-right purple glow

# ── App icon on the right ─────────────────────────────────────────────────────
LOGO_SIZE = 280
logo = Image.open(ICON_SRC).convert("RGBA").resize((LOGO_SIZE, LOGO_SIZE), Image.LANCZOS)

# Soft drop-shadow behind logo
shadow = Image.new("RGBA", (LOGO_SIZE + 40, LOGO_SIZE + 40), (0, 0, 0, 0))
sd = ImageDraw.Draw(shadow)
for r in range(20, 0, -1):
    a = int(80 * (1 - r / 20))
    sd.rounded_rectangle([20-r, 20-r, LOGO_SIZE+20+r, LOGO_SIZE+20+r],
                          radius=40, fill=(249, 115, 22, a))
logo_x = W - LOGO_SIZE - 70
logo_y = (H - LOGO_SIZE) // 2
img.paste(shadow, (logo_x - 20, logo_y - 20), shadow)
img.paste(logo, (logo_x, logo_y), logo)

# ── Fonts ─────────────────────────────────────────────────────────────────────
def ttf(name, size):
    try:
        return ImageFont.truetype(f"C:/Windows/Fonts/{name}.ttf", size)
    except Exception:
        return ImageFont.load_default()

f_app_name  = ttf("arialbd", 58)
f_sub_name  = ttf("arialbd", 13)
f_tagline   = ttf("arialbd", 28)
f_body      = ttf("arial",   19)
f_pill      = ttf("arialbd", 13)
f_footer    = ttf("arial",   11)

# ── Logo row ──────────────────────────────────────────────────────────────────
LX = 60
# Small version of the icon as inline logo mark
icon_sm = Image.open(ICON_SRC).convert("RGBA").resize((52, 52), Image.LANCZOS)
img.paste(icon_sm, (LX, 72), icon_sm)

draw.text((LX + 64, 74),  "SchemeWise",     font=f_app_name,  fill=(255, 255, 255, 255))
draw.text((LX + 66, 137), "NIC · DIGITAL INDIA",  font=f_sub_name,
          fill=(255, 255, 255, 100))

# ── Orange accent rule ────────────────────────────────────────────────────────
draw.rectangle([LX, 165, LX + 300, 169], fill=(249, 115, 22, 220))

# ── Tagline ───────────────────────────────────────────────────────────────────
draw.text((LX, 183), "Find Schemes", font=f_tagline, fill=(255, 255, 255, 230))
draw.text((LX, 217), "You ", font=f_tagline, fill=(255, 255, 255, 230))
# "Deserve" in orange
offset = draw.textlength("You ", font=f_tagline)
draw.text((LX + offset, 217), "Deserve", font=f_tagline, fill=(249, 115, 22, 255))

# ── Body copy ────────────────────────────────────────────────────────────────
draw.text((LX, 268), "AI-powered eligibility matching for 4,200+", font=f_body, fill=(180, 190, 215))
draw.text((LX, 294), "central & state government welfare schemes.", font=f_body, fill=(180, 190, 215))

# ── Feature pills ─────────────────────────────────────────────────────────────
PILLS = ["Instant Eligibility", "AI Powered", "4200+ Schemes"]
px, py = LX, 340
for text in PILLS:
    tw = int(draw.textlength(text, font=f_pill))
    pw, ph = tw + 28, 28
    draw.rounded_rectangle([px, py, px+pw, py+ph],
                            radius=14,
                            fill=(249, 115, 22, 35),
                            outline=(249, 115, 22, 130))
    draw.text((px + 14, py + 7), text, font=f_pill, fill=(253, 186, 116))
    px += pw + 12

# ── Bottom tagline ────────────────────────────────────────────────────────────
footer = "SchemeWise  ·  India's Civic-Tech Platform  ·  2026"
draw.text((LX, H - 28), footer, font=f_footer, fill=(255, 255, 255, 60))

img.convert("RGB").save(
    r"d:\schemewise_2\playstore_policies\feature_graphic.png", "PNG", optimize=True)
print("feature_graphic.png saved (1024×500)")
