# MysterriaTitles audit events

Rows go through the shaded audit client (producer `mysterria-titles`, spool
`plugins/mysterria-audit-spool`), privacy `STAFF_RESTRICTED`. Title-state rows are `OBSERVED`:
they are emitted when the in-memory change is made, and the player's JSON is written later by the
autosave (or on quit and shutdown), as before. Console senders have a null `actorId` and
`actor_name=console`. Tokens and shards carry no audit PDC; shard redemption rows identify the consumed stack by
`material` and `amount`.

| Event type | Outcome | Key facts |
| --- | --- | --- |
| `titles.item.mint_token` | `COMMITTED`; `HIGH` if amount > 64, <= 0, or overflow lost | `/titles admin token give`; audit-only `lot_id` (businessId and correlationId), `actor`, `recipient`, `material`, `amount`, `delivered_inventory`, `overflow_dropped_count`, `overflow_lost_count`, location |
| `titles.item.mint_shard` | as `mint_token` | `/titles admin shard give`; `item=collectioner_shard` |
| `titles.title.staff_grant` | `OBSERVED`; `DENIED` `already_unlocked` | grant, or implicit unlock by `set`; `title_id`, `unlock_method`, `via`, `was_already_unlocked` |
| `titles.title.staff_revoke` | `OBSERVED`; `DENIED` `not_unlocked` | `title_id`, `unlock_method`, `cleared_active` |
| `titles.admin.set_active` | `OBSERVED` | `title_id`, `unlock_method`, `previous_active`; shares correlationId with the implicit `staff_grant` |
| `titles.title.gui_persist_unlock` | `OBSERVED`, reason `permission` | GUI stored a permission-held title; `title_id`, `permission`, `unlock_method`, `test_mode_enabled` |
| `titles.title.gui_clear` | `OBSERVED`, reason `self_clear` | player cleared their own active title in the GUI (actor and subject are the player); `previous_active`, `test_mode_enabled` |
| `titles.title.shard_redeem` | `OBSERVED` | `title_id`, `shards_consumed`, `shards_held_before`, `required_count`, `material`, `amount`, location |
| `titles.progress.add` | `OBSERVED`; `HIGH` if delta <= 0 or > 1 | `title_id`, `delta`, `old_value`, `new_total`, `required`, `unlocked_now`, `flagged_delta` |
| `titles.progress.set` | `OBSERVED` | `title_id`, `old_value`, `new_value`, `required` |
| `titles.title.auto_change` | `OBSERVED` | one row per CoI evaluation with changes; `granted`, `revoked`, counts, `is_beyonder`, `lowest_sequence`, `pathways`, `uniqueness_multiplier`, `trigger`, `trigger_pathway`, `trigger_old_sequence`, `trigger_new_sequence` |
| `titles.admin.test_mode_toggle` | `COMMITTED` | `enabled` (memory-only) |
| `titles.admin.reload` | `COMMITTED` | `titles_count`, `previous_titles_count`, `shard_required_count`, `previous_shard_required_count` |

## Main thread

Rows are built from values already in hand and emitted directly. Dropped because they were item reads done only for a row: `item_sha256` and `distinct_kind_count` on `titles.title.shard_redeem` (a stack copy, serialization and SHA-256 per consumed stack; shards are one known kind) and `display_name` on `mint_token` / `mint_shard` (item meta reads; `item` already names the kind). Also dropped: the extra save per title-state row. The logging used to write the player's JSON right away (a snapshot on the main thread, then an async write) only to tell `COMMITTED` from `FAILED`; the save is back to the autosave alone, and those rows are `OBSERVED`, with no `FAILED` / `persist_failed` row.
