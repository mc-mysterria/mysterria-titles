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

`net.mysterria.titles.api.TitlesService` is registered with Bukkit's ServicesManager while this
plugin is enabled. Call its methods on the main thread:

```java
TitlesService titles = Bukkit.getServicesManager().load(TitlesService.class);
boolean granted = titles.grant(player.getUniqueId(), "the_loyalist", "factions-quest");
```

- `grant(uuid, titleId, source)` uses the normal unlock path, so the data is saved by the usual
  autosave task and name tags refresh as usual. It sends no chat message. It returns `false` for an
  unknown title id, an offline player, or a title already held. Offline players are never changed.
- `has(uuid, titleId)` reads stored unlocks. Offline players return `false`.
- `titleIds()` returns an immutable snapshot of every id defined in `titles.yml`.

`net.mysterria.titles.api.event.TitleGrantedEvent` fires on the main thread after any successful
grant, from the service or from the plugin's own routes. Its getters are `getPlayer()` and
`getPlayerUuid()` (both the player UUID), `getTitleId()` and `getSource()`. The built-in sources are
`admin-grant`, `admin-set`, `progress`, `sequence`, `shard` and `gui`; service calls use the caller's
source, or `api` when it is blank. Plugins without a compile dependency can register a listener by
class name and read the getters reflectively.

## Bonuses

- Only the equipped title's bonus is active. Boosts multiply by `1 + value`; reductions divide by
  `1 + value`, so the felt reduction is `1 - 1/(1 + value)` and that is what the menu shows.
- Durability wear and underwater air are mostly lost one point at a time, so the saved fraction is
  carried per player until it adds up to a whole point. Unbreaking has already rolled when the item
  damage event fires.
- `EntityPotionEffectEvent` cannot change the new effect, so the potion buff cancels it and re-applies
  a longer one, guarded against re-entering itself.
- Saturation is applied after `PlayerItemConsumeEvent`, so the delta is read a tick later.

## Nametags

UnlimitedNameTags polls placeholders on its own interval, which lags behind a title change, so every
change forces a refresh.

## Animated titles

ANIMATED titles pick their frame from wall-clock time, so every client shows the same frame without
per-player animation state.

## Factions church event titles

`keeper_of_the_vigil`, `blessed_by_the_tide`, `reader_of_open_shelves` and `harvest_hand` are granted by Factions
through `TitlesService` for finishing every quest of a church event. They have their own look, not the gold event
banners: the website's accent tag (church colour text on a tint of it over the dark card, a thin outline, rounded
corners), Minecraft's pixel font, and the church's pathway sigil from the website. The art is static PNG at 4x
(`titles/<id>.png`), made by `utils/generate_faction_titles.py`, which also prints the Nexo glyph entries:

```yaml
keeper_of_the_vigil:    { texture: minecraft:titles/keeper_of_the_vigil.png,    ascent: 11, height: 13, offset: -116 }
blessed_by_the_tide:    { texture: minecraft:titles/blessed_by_the_tide.png,    ascent: 11, height: 13, offset: -121 }
reader_of_open_shelves: { texture: minecraft:titles/reader_of_open_shelves.png, ascent: 11, height: 13, offset: -144 }
harvest_hand:           { texture: minecraft:titles/harvest_hand.png,           ascent: 11, height: 13, offset: -90 }
```

They are 2 GUI pixels taller than the event banners so the sigil fits; check the height above the name tag in game.
