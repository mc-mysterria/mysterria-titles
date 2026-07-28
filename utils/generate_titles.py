#!/usr/bin/env python3
"""
Local generator for MysterriaTitles above-nametag banners: same pixel-art
style as https://jeqo.net/gadgets/tag-tool (Jeqo 5 Bit font, gradient
background, white text with an offset shadow bevel), but with a real
moving highlight band (not just a 2-color endpoint swap) since we're not
limited to the web tool's two-color-picker UI anymore.

Usage: python generate_titles.py
Outputs, per title: frames/<id>_frame{0..N}.png and <id>.gif in this folder.
"""

from pathlib import Path
from PIL import Image, ImageDraw, ImageFont

HERE = Path(__file__).parent
FONT_PATH = HERE / "Jeqo-5-Bit.ttf"
FRAMES_DIR = HERE / "frames"

FONT_SIZE = 5
PAD_LEFT = 3
PAD_RIGHT = 3
PAD_TOP = 2
CANVAS_H = 9
TEXT_COLOR = (251, 253, 255, 255)
SHADOW_ALPHA = 108
SHADOW_OFFSET = (1, 1)
TRACKING = 0.14  # small nudge from natural advance; calibrated against the original PNGs

N_FRAMES = 18
FRAME_DELAY_MS = 50  # must be a multiple of 50ms (1 Minecraft tick) and match the plugin's poll rate exactly
BAND_HALF_WIDTH_FRAC = 0.38  # highlight band half-width as a fraction of canvas width
HIGHLIGHT = (235, 235, 245)  # short of pure white, keeps text contrast at the band's peak
# How many full left-to-right sweeps the highlight band makes per loop. Bumping this (rather than
# lowering FRAME_DELAY_MS) is how to make the shimmer feel faster: FRAME_DELAY_MS can't safely go
# below 50ms since Bukkit's scheduler can't refresh faster than 1 tick, and under-sampling the sweep
# aliases into an apparent reverse-direction wobble (wagon-wheel effect). This just makes each frame
# step cover more ground while keeping the same tick-safe poll rate.
SWEEP_CYCLES = 2

TITLES = {
    "august_born": {
        "text": "| AUGUST BORN |",
        "base": "#A64CFF",  # light_purple
    },
    "the_generous": {
        "text": "| THE GENEROUS |",
        "base": "#FFAE0D",  # gold
    },
    "the_loyalist": {
        "text": "| THE LOYALIST |",
        "base": "#4C4CFF",  # blue
    },
    "beyonder": {
        "text": "| BEYONDER |",
        "base": "#880CF2",  # void purple
    },
    "demigod": {
        "text": "| DEMIGOD |",
        "base": "#FF6E0D",  # fire orange
    },
    "saint": {
        "text": "| SAINT |",
        "base": "#2BE4F6",  # holy cyan
    },
    "angel": {
        "text": "| ANGEL |",
        "base": "#4F92F6",  # pale sky blue
    },
    "archangel": {
        "text": "| ARCHANGEL |",
        "base": "#F6D23E",  # radiant gold
    },
    "deity": {
        "text": "| DEITY |",
        "base": "#F21B1B",  # crimson
    },
    "apex_patron": {
        "text": "| APEX PATRON |",
        "base": "#1EF273",  # emerald
    },
    "unique": {
        "text": "| UNIQUE |",
        "base": "#FF4CDB",  # magenta
    },
    "collectioner": {
        "text": "| COLLECTIONER |",
        "base": "#F29D49",  # bronze
    },
    "radiant": {
        "text": "| RADIANT |",
        "base": "#FFED5E",
    },
    "shepherd": {
        "text": "| SHEPHERD |",
        "base": "#69F247",
    },
    "leader": {
        "text": "| LEADER |",
        "base": "#3373F2",
    },
    "developer": {
        "text": "| DEVELOPER |",
        "base": "#FF8E0D",
    },
    "owner": {
        "text": "| OWNER |",
        "base": "#F2B00C",
    },
    "verified": {
        "text": "| VERIFIED |",
        "base": "#2BD2F6",
    },
    "door": {
        "text": "| THE DOOR |",
        "base": "#7272F6",
    },
    "sun": {
        "text": "| THE SUN |",
        "base": "#FFCF0D",
    },
    "tyrant": {
        "text": "| THE TYRANT |",
        "base": "#F20C0C",
    },
    "fool": {
        "text": "| THE FOOL |",
        "base": "#E8FF4D",
    },
    "priest": {
        "text": "| THE PRIEST |",
        "base": "#F6CF58",
    },
    "demoness": {
        "text": "| THE DEMONESS |",
        "base": "#F20CF2",
    },
    "error": {
        "text": "| THE ERROR |",
        "base": "#FF2929",
    },
    "visionary": {
        "text": "| THE VISIONARY |",
        "base": "#2994FF",
    },
    "fortune": {
        "text": "| THE FORTUNE |",
        "base": "#33F233",
    },
    "hanged": {
        "text": "| THE HANGED |",
        "base": "#F27373",
    },
    "darkness": {
        "text": "| THE DARKNESS |",
        "base": "#7B2FFF",
    },
    "paragon": {
        "text": "| THE PARAGON |",
        "base": "#FFDD55",
    },
    "sublunary": {
        "text": "| THE SUBLUNARY |",
        "base": "#6666FF",
    },
    "condenser": {
        "text": "| THE CONDENSER |",
        "base": "#33F2F2",
    },
    "edict": {
        "text": "| THE EDICT |",
        "base": "#F2C40C",
    },
    "chaos": {
        "text": "| THE CHAOS |",
        "base": "#F21B69",
    },
    "chaosmist": {
        "text": "| THE CHAOSMIST |",
        "base": "#F26CF2",
    },
    "patriarch": {
        "text": "| THE PATRIARCH |",
        "base": "#9D49F2",
    },
    "death": {
        "text": "| THE DEATH |",
        "base": "#FF2E63",
    },
    "emperor": {
        "text": "| THE EMPEROR |",
        "base": "#F2960C",
    },
    "moon": {
        "text": "| THE MOON |",
        "base": "#58A7F6",
    },
    "justiciar": {
        "text": "| THE JUSTICIAR |",
        "base": "#408DF2",
    },
    "abyss": {
        "text": "| THE ABYSS |",
        "base": "#0C0CF2",
    },
    "giant": {
        "text": "| THE GIANT |",
        "base": "#F2B373",
    },
    "mother": {
        "text": "| THE MOTHER |",
        "base": "#FF4D6A",
    },
    "hermit": {
        "text": "| THE HERMIT |",
        "base": "#F2A457",
    },
    "chained": {
        "text": "| THE CHAINED |",
        "base": "#C0C0FF",
    },
    "devouring": {
        "text": "| THE DEVOURING |",
        "base": "#F2320C",
    },
    "tower": {
        "text": "| THE TOWER |",
        "base": "#5EA8F2",
    },
    "aeon": {
        "text": "| THE AEON |",
        "base": "#FFDB82",
    },
    "secondlaw": {
        "text": "| THE SECOND LAW |",
        "base": "#73B3F2",
    },
    "everlasting": {
        "text": "| THE EVERLASTING |",
        "base": "#FFFF6E",
    },
}


def hex_to_rgb(h: str) -> tuple[int, int, int]:
    h = h.lstrip("#")
    return tuple(int(h[i:i + 2], 16) for i in (0, 2, 4))


def lerp(a, b, t):
    return tuple(round(a[i] + (b[i] - a[i]) * t) for i in range(3))


def smoothstep(t: float) -> float:
    t = max(0.0, min(1.0, t))
    return t * t * (3 - 2 * t)


def char_positions(text, font, draw):
    """Cumulative float cursor with tracking, rounded per-char to stay pixel-crisp."""
    cursor = 0.0
    positions = []
    for i, ch in enumerate(text):
        positions.append((ch, round(cursor)))
        cursor += draw.textlength(ch, font=font)
        if i < len(text) - 1:
            cursor += TRACKING
    return positions, round(cursor)


def draw_tracked_text(draw_target, text, font, origin, fill):
    tmp = Image.new("RGBA", (1, 1))
    d = ImageDraw.Draw(tmp)
    positions, total_w = char_positions(text, font, d)
    ox, oy = origin
    for ch, x in positions:
        draw_target.text((ox + x, oy), ch, font=font, fill=fill)
    return total_w


def render_band_background(canvas_w: int, canvas_h: int, base, center: float) -> Image.Image:
    half_width = canvas_w * BAND_HALF_WIDTH_FRAC
    img = Image.new("RGBA", (canvas_w, canvas_h), (0, 0, 0, 255))
    for x in range(canvas_w):
        d = abs(x - center)
        d = min(d, canvas_w - d)  # wrap around so the band re-enters on the opposite edge
        t = smoothstep(1.0 - d / half_width)
        col = lerp(base, HIGHLIGHT, t)
        for y in range(canvas_h):
            img.putpixel((x, y), (*col, 255))
    return img


def render_banner(text: str, base_rgb, band_center: float) -> Image.Image:
    font = ImageFont.truetype(str(FONT_PATH), FONT_SIZE)
    tmp = Image.new("RGBA", (1, 1))
    d = ImageDraw.Draw(tmp)
    bbox = d.textbbox((0, 0), text, font=font)
    _, text_w = char_positions(text, font, d)
    canvas_w = PAD_LEFT + text_w + PAD_RIGHT

    img = render_band_background(canvas_w, CANVAS_H, base_rgb, band_center)

    origin_y = PAD_TOP - bbox[1]

    shadow_layer = Image.new("RGBA", (canvas_w, CANVAS_H), (0, 0, 0, 0))
    draw_tracked_text(
        ImageDraw.Draw(shadow_layer), text, font,
        (PAD_LEFT + SHADOW_OFFSET[0], origin_y + SHADOW_OFFSET[1]),
        (0, 0, 0, SHADOW_ALPHA),
    )
    img.alpha_composite(shadow_layer)

    text_layer = Image.new("RGBA", (canvas_w, CANVAS_H), (0, 0, 0, 0))
    draw_tracked_text(ImageDraw.Draw(text_layer), text, font, (PAD_LEFT, origin_y), TEXT_COLOR)
    img.alpha_composite(text_layer)

    return img


def generate(title_id: str, spec: dict):
    base_rgb = hex_to_rgb(spec["base"])

    # render once to get the canvas width, then render each frame's band position
    font = ImageFont.truetype(str(FONT_PATH), FONT_SIZE)
    tmp = Image.new("RGBA", (1, 1))
    d = ImageDraw.Draw(tmp)
    _, text_w = char_positions(spec["text"], font, d)
    canvas_w = PAD_LEFT + text_w + PAD_RIGHT

    frames = []
    for i in range(N_FRAMES):
        center = ((i / N_FRAMES) * SWEEP_CYCLES * canvas_w) % canvas_w
        frames.append(render_banner(spec["text"], base_rgb, center))

    FRAMES_DIR.mkdir(exist_ok=True)
    for i, frame in enumerate(frames):
        frame.save(FRAMES_DIR / f"{title_id}_frame{i}.png")

    gif_frames = [f.convert("P", palette=Image.ADAPTIVE, colors=255) for f in frames]
    gif_frames[0].save(
        HERE / f"{title_id}.gif",
        save_all=True,
        append_images=gif_frames[1:],
        duration=FRAME_DELAY_MS,
        loop=0,
        disposal=2,
    )
    print(f"{title_id}: {len(frames)} frames, {frames[0].size} -> {title_id}.gif")


if __name__ == "__main__":
    for title_id, spec in TITLES.items():
        generate(title_id, spec)
