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

## Bonuses

- Only the equipped title's bonus is active. Boosts multiply by `1 + value`; reductions divide by
  `1 + value`, so the felt reduction is `1 - 1/(1 + value)` and that is what the menu shows.
- Durability wear and underwater air are mostly lost one point at a time, so the saved fraction is
  carried per player until it adds up to a whole point. Unbreaking has already rolled when the item
  damage event fires.
- `EntityPotionEffectEvent` cannot change the new effect, so the potion buff cancels it and re-applies
  a longer one, guarded against re-entering itself.
- Saturation is applied after `PlayerItemConsumeEvent`, so the delta is read a tick later.
- Storm lightning is told apart by tagging `WEATHER` strikes on `LightningStrikeEvent`. CoI spells and
  artifact drawbacks strike real lightning bolts, so checking only the `LIGHTNING` damage cause would
  also soften magic in PvP.

## Nametags

UnlimitedNameTags polls placeholders on its own interval, which lags behind a title change, so every
change forces a refresh.

## Animated titles

ANIMATED titles pick their frame from wall-clock time, so every client shows the same frame without
per-player animation state.
