"""The /titles window. It covers the hidden player inventory too (the plugin swaps in an empty lower gui), so
slots >= 54 are the lower area: L0-L1 hold Collection / Equipped / Filter, L2-L3 the Previous / Close / Next deck.

    python tools/gui/titles.py            # previews into %TEMP%/titles-gui
    python tools/gui/titles.py --write    # also the PNG, icons and glyphs/titles.yml in mysterria-resources

The slot constants here and in gui/Layout.java must agree.
"""
import os
import pathlib
import sys

sys.path.insert(0, os.path.expanduser(r"~\.claude\skills\custom-gui\scripts"))
from guikit import *

ROOT = pathlib.Path(__file__).resolve().parents[2]
LIBRARY = pathlib.Path(os.path.expanduser(r'~\.claude\skills\custom-gui\library'))
NS = 'titles'
GLYPH = 'titles_hall'
WRITE = '--write' in sys.argv
OUT = os.environ.get('OUT', os.path.join(os.environ.get('TEMP', '.'), 'titles-gui'))
TEX = os.path.join(MRP, 'assets', NS, 'textures', 'gui')
GLYPHS = os.path.join(RES, 'glyphs', 'titles.yml')
SHIFT, WIDTH = -15, 190
TEXT = '#f0e3b8'

NIGHT = ramp('#04050a', '#080a14', '#10132a', '#1a1d3d', '#272a55', '#3a3d72')
VELVET = ramp('#0b0714', '#1c1033', '#2e1a52', '#432870', '#5f3c96', '#8462bf')
AMETHYST = ramp('#1a0b2a', '#3a1a5c', '#5b2a8c', '#7d43b8', '#a16ad6', '#c9a0ee')
GILT = ramp('#1f1606', '#4a3610', '#7a5a18', '#b8860b', '#c8b273', '#ecdfae')

ICONS.update({
    'big_left': [
        "......##......",
        ".....###......",
        "....####......",
        "...##########.",
        "..###########.",
        ".############.",
        "..###########.",
        "...##########.",
        "....####......",
        ".....###......",
        "......##......",
    ],
    'big_cross': [
        "##........##",
        "###......###",
        ".###....###.",
        "..###..###..",
        "...######...",
        "....####....",
        "...######...",
        "..###..###..",
        ".###....###.",
        "###......###",
        "##........##",
    ],
    'crown': [
        "..........##..........",
        ".........####.........",
        "##........##........##",
        "###......####......###",
        "####....######....####",
        "#####..########..#####",
        "######################",
        "######################",
        "######################",
        ".####################.",
        ".####################.",
        "......................",
        ".####################.",
        ".####################.",
    ],
    'tag': [
        "...#########",
        "..##########",
        ".###########",
        "##.#########",
        ".###########",
        "..##########",
        "...#########",
    ],
})
ICONS['big_right'] = [row[::-1] for row in ICONS['big_left']]

ROWS = 6
LOW = 9 * ROWS
COLLECTION, EQUIPPED, FILTER = LOW, LOW + 3, LOW + 6
EQUIPPED_PEDESTAL = EQUIPPED + 1
PREV, CLOSE, NEXT = LOW + 18, LOW + 21, LOW + 24


def window():
    g = Gui(rows=ROWS, seed=31)
    b = full_board(ROWS)
    inner = g.frame(b, VELVET, 'flat', t=6, speckle=.1)
    x, _, w, _ = inner
    g.rect((x, inner[1], w, 16 - inner[1]), NIGHT[B])
    g.rect((x, 15, w, 1), GILT[S])
    g.rect((x, 16, w, 1), GILT[O])
    top = 17 + 18 * ROWS
    g.panel((x, 17, w, top - 17), NIGHT, 'night')
    g.rect((x, top, w, 1), GILT[O])
    g.rect((x, top + 1, w, 1), GILT[S])
    g.panel((x, top + 2, w, inner[1] + inner[3] - top - 2), VELVET, 'flat', speckle=.08)
    return g


def niches(g):
    for slot in range(LOW):
        g.well(grow(slot_rect(slot, ROWS), -1), VELVET)


def card(g, rect, back=AMETHYST, frame=GILT, icon=None, icon_rp=GILT, picture=None):
    x, y, w, h = rect
    g.shade((x + 1, y + h, w - 1, 1), .5)
    inner = g.box(rect, frame, 'flat')
    ix, iy, iw, ih = inner
    for j in range(ih):
        t = j / max(1, ih - 1)
        g.rect((ix, iy + j, iw, 1), back[L if t < .35 else B if t < .7 else S])
    if picture is not None:
        g.paste(picture, ix + (iw - picture.width) // 2, iy + (ih - picture.height) // 2)
    if icon:
        ic = icon_image(icon, icon_rp)
        g.paste(ic, ix + (iw - ic.width) // 2, iy + (ih - ic.height) // 2)
    return inner


def big(g, top_left, cols, rows_, **kw):
    return card(g, block(top_left, cols, rows_, ROWS), **kw)


def pedestal(g, slot):
    r = slot_rect(slot, ROWS)
    g.box(r, GILT, 'flat', raised=False)
    g.well(grow(r, -2), NIGHT)


def lens():
    return snap(Image.open(LIBRARY / 'props' / 'emp_lens.png').convert('RGBA'), GILT, AMETHYST, PARCHMENT, NIGHT)


def nameplate(g, rect):
    x, y, w, h = rect
    plate = (x + 6, y + h - 14, w - 12, 9)
    g.shade((plate[0] + 1, plate[1] + plate[3], plate[2] - 1, 1), .5)
    g.box(plate, GILT, 'flat')


def hall():
    g = window()
    niches(g)
    big(g, COLLECTION, 3, 2, icon='crown')
    nameplate(g, big(g, EQUIPPED, 3, 2, back=VELVET))
    pedestal(g, EQUIPPED_PEDESTAL)
    big(g, FILTER, 3, 2, picture=lens())
    big(g, PREV, 3, 2, icon='big_left', icon_rp=PARCHMENT)
    big(g, CLOSE, 3, 2, icon='big_cross', back=RED, icon_rp=PARCHMENT)
    big(g, NEXT, 3, 2, icon='big_right', icon_rp=PARCHMENT)
    return g


def write_glyph(entry):
    keep, key = [], None
    if os.path.exists(GLYPHS):
        for line in open(GLYPHS, encoding='utf-8').read().splitlines():
            if line and not line.startswith(('#', ' ')):
                key = line.rstrip(':')
            if key != GLYPH:
                keep.append(line)
    with open(GLYPHS, 'w', encoding='utf-8', newline='\n') as fh:
        fh.write('\n'.join(keep).rstrip() + '\n\n' + entry)


def run():
    os.makedirs(OUT, exist_ok=True)
    g = hall()
    g.outline()
    geo = g.save(os.path.join(TEX if WRITE else OUT, 'hall.png'))
    assert (geo['shift'], geo['w']) == (SHIFT, WIDTH), f'left the shared board: {geo}'
    entry = g.glyph_yml(GLYPH, f'{NS}:gui/hall')
    items = {s: 'item:name_tag' for s in range(20)}
    items.update({s: 'item:barrier' for s in range(20, 30)})
    items[EQUIPPED_PEDESTAL] = 'item:diamond'
    g.preview(os.path.join(OUT, 'hall_preview.png'), items, title='Titles  12/58', title_color=TEXT,
              title_cx=8 + (font().width('Titles  12/58') - 1) // 2, hover=EQUIPPED_PEDESTAL)
    if WRITE:
        write_glyph(entry)
        write_blank(NS)
        write_item(NS, 'gui/lock', item_icon('lock', GRAY))
        write_item(NS, 'gui/no_title', item_icon('tag', GRAY))
        write_tooltip_style(NS, 'hall', (16, 10, 30, 240), AMETHYST[L], GILT[S])
    print(entry)
    print('text shift', 8 - (WIDTH + 1 - geo['ox']))


if __name__ == '__main__':
    run()
