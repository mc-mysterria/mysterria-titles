#!/usr/bin/env python3
"""
Generator for the framed event titles (The Ragnarok, Stormblessed, Watcher of Light and
Darkness). Same font, size, frame count and shimmer timing as generate_titles.py, but with a
checkered body, a 1px rim with corner knobs, coloured lettering with a dark shadow, and a
fixed 1px gap between glyphs so long words do not drift apart.

Usage: python generate_framed_titles.py
Outputs <id>.gif next to this script. Dragoness uses the plain style from generate_titles.py.
"""

from PIL import Image, ImageDraw, ImageFont

import generate_titles as g

font = ImageFont.truetype(str(g.FONT_PATH), g.FONT_SIZE)
GAP = 1        # pixels between glyphs
SPACE = 3      # pixels for a space
PAD = 4        # pixels between the rim and the text on each side
H = 9          # canvas height, matches every other title

GOLD_RIM = (226, 178, 58)
GOLD_TEXT = (242, 199, 68)
WHITE = (244, 240, 255)
YELLOW = (255, 230, 64)
PURPLE = (178, 102, 255)


def glyph(ch):
    im = Image.new("RGBA", (20, 20), (0, 0, 0, 0))
    ImageDraw.Draw(im).text((2, 2), ch, font=font, fill=(255, 255, 255, 255))
    bb = im.getbbox()
    return im.crop(bb)


def layout(words):
    """words: list of (text, colour). Returns [(x, glyph, colour)], total width."""
    parts, x = [], 0
    for wi, (word, colour) in enumerate(words):
        for ch in word:
            if ch == " ":
                x += SPACE
                continue
            gl = glyph(ch)
            parts.append((x, gl, colour))
            x += gl.width + GAP
        if wi < len(words) - 1:
            x += SPACE - GAP
    return parts, x - GAP


def banner(title_id, words, body, body2, rim, shadow, band=(255, 236, 160)):
    parts, tw = layout(words)
    th = max(gl.height for _, gl, _ in parts)
    W = tw + 1 + 2 * PAD + 2
    oy = 1 + (H - 2 - (th + 1) + 1) // 2
    ox = 1 + PAD
    frames = []
    for f in range(g.N_FRAMES):
        im = Image.new("RGBA", (W, H), (0, 0, 0, 0))
        d = ImageDraw.Draw(im)
        for y in range(1, H - 1):
            for x in range(1, W - 1):
                d.point((x, y), body if ((x // 2) + (y // 2)) % 2 == 0 else body2)
        centre = ((f / g.N_FRAMES) * g.SWEEP_CYCLES) % 1.0 * (W + 20) - 10
        for x in range(1, W - 1):
            t = max(0.0, 1 - abs(x - centre) / (W * 0.18))
            if t > 0:
                for y in range(1, H - 1):
                    p = im.getpixel((x, y))
                    a = g.smoothstep(t) * 0.35
                    d.point((x, y), tuple(int(p[i] * (1 - a) + band[i] * a) for i in range(3)) + (255,))
        d.rectangle([0, 0, W - 1, H - 1], outline=rim)
        for (x, y) in [(0, 0), (W - 2, 0), (0, H - 2), (W - 2, H - 2)]:
            d.rectangle([x, y, x + 1, y + 1], fill=rim)
        for x, gl, colour in parts:
            for dx, dy, c in [(1, 1, shadow), (0, 0, colour)]:
                tint = Image.new("RGBA", gl.size, c + (255,))
                im.paste(tint, (ox + x + dx, oy + dy), gl)
        frames.append(im)
    frames[0].save(g.HERE / f"{title_id}.gif", save_all=True, append_images=frames[1:],
                   duration=g.FRAME_DELAY_MS, loop=0, disposal=2)
    print(f"{title_id}: {len(frames)} frames, {frames[0].size} -> {title_id}.gif")


TITLES = {
    "the_ragnarok": dict(
        words=[("| THE RAGNAROK |", GOLD_TEXT)],
        body=(118, 14, 14), body2=(138, 20, 20), rim=GOLD_RIM, shadow=(46, 8, 8)),
    "stormblessed": dict(
        words=[("| STORMBLESSED |", (214, 238, 255))],
        body=(16, 52, 120), body2=(20, 62, 140), rim=(120, 190, 255), shadow=(6, 18, 50),
        band=(220, 240, 255)),
    "watcher_of_light_and_darkness": dict(
        words=[("| WATCHER OF", WHITE), ("LIGHT", YELLOW), ("AND", WHITE), ("DARKNESS", PURPLE), ("|", WHITE)],
        body=(34, 14, 54), body2=(44, 20, 66), rim=GOLD_RIM, shadow=(20, 6, 30)),
}

if __name__ == "__main__":
    for title_id, spec in TITLES.items():
        banner(title_id, **spec)
