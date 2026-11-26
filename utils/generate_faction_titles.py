#!/usr/bin/env python3
"""
Generator for the Factions church event titles (keeper_of_the_vigil, blessed_by_the_tide, reader_of_open_shelves,
harvest_hand). They have their own look, not the gold event banners: the website's accent tag (church colour text on a
14% tint of it over the dark card #131318, a 42% outline, rounded corners), Minecraft's own pixel font, and the
pathway sigil from the website (mysteria-frontend src/assets/images/pathways/<pathway>.webp) with its dark medallion
keyed out. Drawn at 4x so the sigil keeps its detail; the Nexo glyph shows it 13 GUI pixels high.

Usage: python generate_faction_titles.py <minecraft ascii.png> <website pathways folder>
  ascii.png is assets/minecraft/textures/font/ascii.png from a Minecraft client jar.
Writes ../titles/<id>.png and prints the Nexo glyph entries.
"""
import sys
from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw

HERE = Path(__file__).parent
OUT = HERE.parent / "titles"
F = 4                      # texture pixels per GUI pixel
CARD = (0x13, 0x13, 0x18)  # website --arc-card
TITLES = [
    ("keeper_of_the_vigil", "Keeper of the Vigil", (0x98, 0xA2, 0xFF), "darkness"),     # Evernight
    ("blessed_by_the_tide", "Blessed by the Tide", (0x4D, 0x9E, 0xFF), "tyrant"),       # Lord of Storms
    ("reader_of_open_shelves", "Reader of Open Shelves", (0xC3, 0x8D, 0xFF), "hermit"),  # Knowledge and Wisdom
    ("harvest_hand", "Harvest Hand", (0x5F, 0xD3, 0x8D), "mother"),                   # Earth Mother
]


def mix(a, b, t):
    return tuple(round(p + (q - p) * t) for p, q in zip(a, b))


class Font:
    def __init__(self, path):
        self.img = Image.open(path).convert("RGBA")

    def glyph(self, ch):
        code = ord(ch)
        cell = self.img.crop(((code % 16) * 8, (code // 16) * 8, (code % 16) * 8 + 8, (code // 16) * 8 + 8))
        cols = [x for x in range(8) for y in range(8) if cell.getpixel((x, y))[3] > 0]
        return cell, (max(cols) + 1) if cols else 3

    def width(self, s):
        return sum(4 if c == " " else self.glyph(c)[1] + 1 for c in s) - 1

    def draw(self, img, x, y, s, color):
        px = img.load()
        for c in s:
            if c == " ":
                x += 4
                continue
            cell, w = self.glyph(c)
            for gx in range(w):
                for gy in range(8):
                    if cell.getpixel((gx, gy))[3] > 0:
                        px[x + gx, y + gy] = color + (255,)
            x += w + 1


def sigil(path, accent, size):
    a = np.asarray(Image.open(path).convert("RGBA")).astype(float)
    lum = 0.299 * a[..., 0] + 0.587 * a[..., 1] + 0.114 * a[..., 2]
    keep = np.clip((lum - 95) / 115, 0, 1) * (a[..., 3] / 255)
    ys, xs = np.where(keep > 0.35)
    side = max(xs.max() + 1 - xs.min(), ys.max() + 1 - ys.min())
    cx, cy = (xs.min() + xs.max() + 1) // 2, (ys.min() + ys.max() + 1) // 2
    glow = np.clip(lum / 255, 0, 1) ** 2
    rgb = np.stack([accent[c] + (255 - accent[c]) * 0.55 * glow for c in range(3)], axis=-1)
    img = Image.fromarray(np.dstack([rgb, keep * 255]).astype(np.uint8), "RGBA")
    img = img.crop((cx - side // 2, cy - side // 2, cx - side // 2 + side, cy - side // 2 + side))
    return img.resize((size, size), Image.LANCZOS)


def badge(font, title, accent, sigil_path):
    gw, gh = 2 + 11 + 3 + font.width(title) + 4, 13
    img = Image.new("RGBA", (gw * F, gh * F), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.rounded_rectangle((0, 0, gw * F - 1, gh * F - 1), radius=2 * F, fill=mix(CARD, accent, 0.42) + (255,))
    d.rounded_rectangle((F, F, gw * F - 1 - F, gh * F - 1 - F), radius=F + F // 2, fill=mix(CARD, accent, 0.14) + (255,))
    text = Image.new("RGBA", (gw, gh), (0, 0, 0, 0))
    font.draw(text, 16, 3, title, accent)
    img.alpha_composite(text.resize((gw * F, gh * F), Image.NEAREST))
    img.alpha_composite(sigil(sigil_path, accent, 11 * F), (2 * F, F))
    return img, gw


if __name__ == "__main__":
    font = Font(sys.argv[1])
    web = Path(sys.argv[2])
    print("# Nexo glyphs (glyphs/titles.yml); offset follows the existing banners (width + 3, negative)")
    for key, title, accent, pathway in TITLES:
        img, gw = badge(font, title, accent, web / f"{pathway}.webp")
        img.save(OUT / f"{key}.png")
        print(f"{key}:\n  texture: minecraft:titles/{key}.png\n  ascent: 11\n  height: 13\n  offset: -{gw + 3}\n")
