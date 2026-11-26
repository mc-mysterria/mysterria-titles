# MysterriaTitles design notes

The reasoning that used to live in javadoc.

## Startup order with Circle of Imagination

- CoI is a `paper-plugin.yml` plugin, so it always finishes enabling after every `plugin.yml` plugin,
  whatever `softdepend` says. `registerCoiIntegration()` runs once in `onEnable` and again from
  `CoiAvailabilityListener` on CoI's `PluginEnableEvent`; it is idempotent.
- The buff registry is validated on `ServerLoadEvent` (`BuffRegistryValidationListener`), after every
  plugin of either format is up. Validating in `onEnable` would flag every CoI-tied title as inert.
- `MagicDamageBoostBuff`, `MagicDefenseBoostBuff`, `MadnessReductionBuff` and `CoiTitleListener` take CoI
  event types as handler parameters. Bukkit's reflective handler scan throws `NoClassDefFoundError` if
  they are registered without CoI, so they are only registered inside `registerCoiIntegration()`.
- No CoI event covers uniqueness-trait changes, so a 10-second recheck task re-evaluates sequence
  titles as a safety net (also catches changes made while the player was offline).

## Unlocking

- Effective unlock = stored unlocked set ∪ PERMISSION titles whose permission the player holds.
  COMMAND and AUTO both rely on the stored set; AUTO only changes the hint shown on the locked card.
- Sequence titles are independent thresholds: a Sequence 2 Beyonder holds demigod (≤4), saint (≤3)
  and angel (≤2) at once. Pathway titles mirror CoI pathway names 1:1. Auto-granted titles are revoked
  again when the player stops qualifying; they are never force-equipped.
- Progress titles (`unlock.progress-required`) unlock once the stored counter reaches the threshold.
  The Loyalist gets one point per Advent Calendar day claimed, so it proves every day was claimed.
  `progress set` is an admin correction and never auto-unlocks.
- Collectioner needs the full configured set of shards in the inventory; right-clicking one redeems them.
- Anniversary Tokens are minted only here so every source produces an item identical to the festive
  shop's payment item (BrilliantEmporium `seasonal-shops/anniversary_2026.yml`).
- Test mode is in-memory only and cleared on quit; it never writes to the player's data.

## Public hook for other plugins

`net.mysterria.titles.api.TitlesService` is registered with Bukkit's ServicesManager. On the main thread, `grant(uuid, titleId, source)` unlocks a title through the normal path (no chat message; false for an unknown id, an offline player or a title already held), and `has(uuid, titleId)` reads stored unlocks.

`net.mysterria.titles.api.event.TitleGrantedEvent` fires on the main thread after any successful grant, with `getPlayerUuid()`, `getTitleId()` and `getSource()`.

## Factions church event titles

`keeper_of_the_vigil`, `blessed_by_the_tide`, `reader_of_open_shelves` and `harvest_hand` are granted by Factions for finishing a church event. Their art is in `titles/<id>.png`; the Nexo glyph entries:

```yaml
keeper_of_the_vigil:    { texture: minecraft:titles/keeper_of_the_vigil.png,    ascent: 11, height: 13, offset: -116 }
blessed_by_the_tide:    { texture: minecraft:titles/blessed_by_the_tide.png,    ascent: 11, height: 13, offset: -121 }
reader_of_open_shelves: { texture: minecraft:titles/reader_of_open_shelves.png, ascent: 11, height: 13, offset: -144 }
harvest_hand:           { texture: minecraft:titles/harvest_hand.png,           ascent: 11, height: 13, offset: -90 }
```
