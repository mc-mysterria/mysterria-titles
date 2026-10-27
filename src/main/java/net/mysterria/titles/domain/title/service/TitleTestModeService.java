package net.mysterria.titles.domain.title.service;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TitleTestModeService {

    private final Set<UUID> enabled = ConcurrentHashMap.newKeySet();

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
