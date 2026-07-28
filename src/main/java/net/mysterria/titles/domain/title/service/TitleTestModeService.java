package net.mysterria.titles.domain.title.service;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Purely in-memory "show every title as unlocked" toggle for admins previewing titles.yml
 * changes in the GUI. Never touches PlayerTitleData or the JSON store, so it can't leak into a
 * real unlock - it only lives in this set, which starts empty on every plugin (re)start and is
 * cleared per-player on quit.
 */
public class TitleTestModeService {

    private final Set<UUID> enabled = ConcurrentHashMap.newKeySet();

    /**
     * @return true if test mode is now enabled for the player, false if it was just disabled
     */
    public boolean toggle(UUID playerId) {
        if (!enabled.remove(playerId)) {
            enabled.add(playerId);
            return true;
        }
        return false;
    }

    public boolean isEnabled(UUID playerId) {
        return enabled.contains(playerId);
    }

    public void disable(UUID playerId) {
        enabled.remove(playerId);
    }
}
