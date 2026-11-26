#!/usr/bin/env python3
"""
Generator for the Factions church event titles (keeper_of_the_vigil, blessed_by_the_tide, reader_of_open_shelves,
harvest_hand). They have their own look, not the gold event banners: the website's accent tag (church colour text on a
14% tint of it over the dark card #131318, a 42% outline, rounded corners), Minecraft's own pixel font, and the
church's official sacred emblem from the novel (Lord of the Mysteries wiki, "Sacred Emblem - Church of the ...").
Drawn at 4x so the emblem keeps its detail; the Nexo glyph shows it 13 GUI pixels high.

Usage: python generate_faction_titles.py <minecraft ascii.png> <emblems folder>
  ascii.png is assets/minecraft/textures/font/ascii.png from a Minecraft client jar.
  The emblems folder holds official_<church>.png (transparent PNGs).
Writes ../titles/<id>.png and prints the Nexo glyph entries.
"""
import sys
from pathlib import Path

from PIL import Image, ImageDraw

HERE = Path(__file__).parent
OUT = HERE.parent / "titles"
F = 4                      # texture pixels per GUI pixel
CARD = (0x13, 0x13, 0x18)  # website --arc-card
TITLES = [  # id, text, church colour (its pathway's website colour), official emblem file
    ("keeper_of_the_vigil", "Keeper of the Vigil", (0x98, 0xA2, 0xFF), "official_evernight_goddess.png"),
    ("blessed_by_the_tide", "Blessed by the Tide", (0x4D, 0x9E, 0xFF), "official_lord_of_storms.png"),
    ("reader_of_open_shelves", "Reader of Open Shelves", (0xC3, 0x8D, 0xFF), "official_god_of_knowledge_and_wisdom.png"),
    ("harvest_hand", "Harvest Hand", (0x5F, 0xD3, 0x8D), "official_earth_mother.png"),
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


def emblem(path, size):
    """The official emblem, trimmed to its drawing and centred on a square, in its own colours."""
    img = Image.open(path).convert("RGBA")
    img = img.crop(img.getbbox())
    side = max(img.size)
    square = Image.new("RGBA", (side, side), (0, 0, 0, 0))
    square.alpha_composite(img, ((side - img.width) // 2, (side - img.height) // 2))
    return square.resize((size, size), Image.LANCZOS)


def badge(font, title, accent, emblem_path):
    gw, gh = 2 + 11 + 3 + font.width(title) + 4, 13
    img = Image.new("RGBA", (gw * F, gh * F), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.rounded_rectangle((0, 0, gw * F - 1, gh * F - 1), radius=2 * F, fill=mix(CARD, accent, 0.42) + (255,))
    d.rounded_rectangle((F, F, gw * F - 1 - F, gh * F - 1 - F), radius=F + F // 2, fill=mix(CARD, accent, 0.14) + (255,))
    text = Image.new("RGBA", (gw, gh), (0, 0, 0, 0))
    font.draw(text, 16, 3, title, accent)
    img.alpha_composite(text.resize((gw * F, gh * F), Image.NEAREST))
    img.alpha_composite(emblem(emblem_path, 11 * F), (2 * F, F))
    return img, gw


if __name__ == "__main__":
    font = Font(sys.argv[1])
    emblems = Path(sys.argv[2])
    print("# Nexo glyphs (glyphs/titles.yml); offset follows the existing banners (width + 3, negative)")
    for key, title, accent, emblem_file in TITLES:
        img, gw = badge(font, title, accent, emblems / emblem_file)
        img.save(OUT / f"{key}.png")
        print(f"{key}:\n  texture: minecraft:titles/{key}.png\n  ascent: 11\n  height: 13\n  offset: -{gw + 3}\n")
