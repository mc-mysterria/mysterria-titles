#!/usr/bin/env python3
"""
Banners for the Mystical Weaponry gun titles (unlocked by the mysticalweaponry.title.<id> permissions that plugin
hands out). Mastery, collection and grind titles use the plain shimmer of generate_titles.py; the mythic skin titles
(a mythic skin raised to stage III) and the obsession titles use the framed style of generate_framed_titles.py, in
the colours of their skin.

Usage: python generate_gun_titles.py
Outputs <id>.gif next to this script (move them to titles/).
"""

import shutil

import generate_framed_titles as framed
import generate_titles as plain

GOLD_RIM = framed.GOLD_RIM
GOLD_TEXT = framed.GOLD_TEXT

PLAIN = {
    # highest mastery with each gun
    "repeater_virtuoso": ("| REPEATER VIRTUOSO |", "#8A93A8"),
    "bell_ringer": ("| THE BELL-RINGER |", "#4F8FA6"),
    "steam_marksman": ("| STEAM MARKSMAN |", "#B87A2E"),
    "scattershot": ("| SCATTERSHOT |", "#5E8F2E"),
    "lever_and_buckshot": ("| LEVER AND BUCKSHOT |", "#C9902C"),
    "winterborn": ("| WINTERBORN |", "#4A86C2"),
    "master_gunner": ("| MASTER GUNNER |", "#5C6678"),
    "the_inevitable": ("| THE INEVITABLE |", "#5FA03A"),
    # collection
    "gun_collector": ("| GUN COLLECTOR |", "#B08A4A"),
    "curator_of_arms": ("| CURATOR OF ARMS |", "#B8964E"),
    "armory_archivist": ("| ARMORY ARCHIVIST |", "#9A7A44"),
    "connoisseur": ("| THE CONNOISSEUR |", "#7A4FD6"),
    "well_rounded": ("| WELL-ROUNDED |", "#5A8FC2"),
    # grind
    "sharpshooter": ("| SHARPSHOOTER |", "#8C8C96"),
    "gunhand": ("| GUNHAND |", "#A0845C"),
    "veteran_shooter": ("| VETERAN SHOOTER |", "#8A6A3A"),
    "long_arm_of_the_law": ("| LONG ARM OF THE LAW |", "#5A7FA6"),
    "beyonder_hunter": ("| BEYONDER HUNTER |", "#8A4FC2"),
    "duelist": ("| THE DUELIST |", "#C24A3A"),
    "bounty_hunter": ("| BOUNTY HUNTER |", "#A6743A"),
    "contract_veteran": ("| CONTRACT VETERAN |", "#7A5A30"),
}

FRAMED = {
    # a mythic skin at stage III: the skin's colours
    "crimson_moon_hunter": dict(
        words=[("| CRIMSON MOON HUNTER |", (230, 226, 220))],
        body=(110, 10, 18), body2=(130, 16, 24), rim=(200, 20, 30), shadow=(40, 4, 8), band=(255, 170, 170)),
    "orrery_keeper": dict(
        words=[("| KEEPER OF THE ORRERY |", (246, 214, 128))],
        body=(40, 50, 110), body2=(50, 60, 128), rim=GOLD_RIM, shadow=(14, 18, 44)),
    "long_night_herald": dict(
        words=[("| HERALD OF THE", (236, 248, 255)), ("LONG NIGHT", (240, 90, 100)), ("|", (236, 248, 255))],
        body=(30, 56, 92), body2=(38, 68, 108), rim=(120, 172, 214), shadow=(10, 20, 36), band=(220, 240, 255)),
    "pale_gate_warden": dict(
        words=[("| WARDEN OF THE PALE GATE |", (240, 236, 224))],
        body=(42, 38, 48), body2=(52, 48, 60), rim=(180, 220, 230), shadow=(16, 14, 20), band=(220, 240, 245)),
    "above_the_fog": dict(
        words=[("| ABOVE THE GRAY FOG |", (230, 232, 240))],
        body=(74, 76, 86), body2=(86, 88, 100), rim=(222, 182, 92), shadow=(28, 28, 34)),
    "higdons_reflection": dict(
        words=[("| HIGDON'S REFLECTION |", (255, 255, 255))],
        body=(56, 84, 130), body2=(66, 98, 150), rim=(200, 220, 245), shadow=(18, 28, 48), band=(230, 240, 255)),
    "higdons_inevitability": dict(
        words=[("| HIGDON'S INEVITABILITY |", (230, 255, 210))],
        body=(40, 62, 34), body2=(50, 76, 42), rim=(106, 138, 90), shadow=(14, 22, 12), band=(220, 255, 200)),
    "locust_sovereign": dict(
        words=[("| LOCUST SOVEREIGN |", (230, 170, 50))],
        body=(30, 42, 26), body2=(40, 56, 34), rim=(230, 170, 50), shadow=(12, 16, 10)),
    # collection and obsession
    "keeper_of_myths": dict(
        words=[("| KEEPER OF MYTHS |", GOLD_TEXT)],
        body=(92, 58, 20), body2=(108, 70, 26), rim=GOLD_RIM, shadow=(36, 20, 6)),
    "arsenal_master": dict(
        words=[("| MASTER OF THE ARSENAL |", GOLD_TEXT)],
        body=(60, 44, 24), body2=(72, 54, 30), rim=GOLD_RIM, shadow=(24, 16, 8)),
    "gun_mad": dict(
        words=[("| MAD FOR GUNS |", (255, 160, 110))],
        body=(110, 24, 14), body2=(128, 32, 18), rim=(255, 138, 92), shadow=(40, 8, 4), band=(255, 200, 160)),
    "the_unblinking": dict(
        words=[("| THE UNBLINKING |", (255, 255, 255))],
        body=(50, 52, 58), body2=(62, 64, 72), rim=(200, 204, 212), shadow=(16, 16, 20), band=(240, 240, 250)),
    "horizon_reaper": dict(
        words=[("| HORIZON REAPER |", (207, 228, 255))],
        body=(34, 50, 84), body2=(42, 62, 100), rim=(120, 160, 210), shadow=(12, 18, 32), band=(220, 236, 255)),
    "bane_of_beyonders": dict(
        words=[("| BANE OF", (240, 220, 255)), ("BEYONDERS", (200, 130, 255)), ("|", (240, 220, 255))],
        body=(50, 16, 80), body2=(62, 22, 98), rim=(176, 124, 224), shadow=(20, 6, 32)),
    "man_hunter": dict(
        words=[("| THE MANHUNTER |", (255, 170, 160))],
        body=(84, 8, 14), body2=(100, 12, 20), rim=(200, 60, 60), shadow=(32, 2, 6), band=(255, 180, 170)),
    "bounty_obsessed": dict(
        words=[("| LIVES FOR THE BOARD |", GOLD_TEXT)],
        body=(80, 50, 22), body2=(94, 60, 28), rim=GOLD_RIM, shadow=(30, 18, 6)),
    "undying_myth": dict(
        words=[("| UNDYING MYTH |", (255, 242, 200))],
        body=(100, 46, 14), body2=(118, 56, 18), rim=GOLD_RIM, shadow=(40, 16, 4), band=(255, 236, 190)),
    "one_with_every_gun": dict(
        words=[("| ONE WITH", (255, 255, 255)), ("EVERY GUN", GOLD_TEXT), ("|", (255, 255, 255))],
        body=(40, 30, 16), body2=(52, 40, 22), rim=GOLD_RIM, shadow=(16, 10, 4), band=(255, 240, 200)),
}

if __name__ == "__main__":
    for title_id, (text, base) in PLAIN.items():
        plain.generate(title_id, {"text": text, "base": base})
    # generate_titles.py also writes every frame as a PNG; only the GIFs are kept
    shutil.rmtree(plain.FRAMES_DIR, ignore_errors=True)
    for title_id, spec in FRAMED.items():
        framed.banner(title_id, **spec)
