package net.mysterria.titles.service;

import net.mysterria.titles.model.PlayerTitleData;
import net.mysterria.titles.model.Title;
import net.mysterria.titles.registry.TitleRegistry;
import net.mysterria.titles.storage.PlayerDataManager;

import java.util.Optional;
import java.util.UUID;

public final class TitleBonusService {

    private final PlayerDataManager playerDataManager;
    private final TitleRegistry titleRegistry;

    public TitleBonusService(PlayerDataManager playerDataManager, TitleRegistry titleRegistry) {
        this.playerDataManager = playerDataManager;
        this.titleRegistry = titleRegistry;
    }

    public Optional<Title> getActiveTitle(UUID uuid) {
        PlayerTitleData data = playerDataManager.getCached(uuid);
        if (data == null) return Optional.empty();
        return data.getActiveTitle().flatMap(titleRegistry::get);
    }

    public double getMultiplier(UUID uuid, String buffId) {
        return getActiveTitle(uuid)
                .filter(title -> title.bonus().type().equals(buffId))
                .map(title -> 1.0 + title.bonus().value())
                .orElse(1.0);
    }

    public double apply(UUID uuid, String buffId, double base) {
        return base * getMultiplier(uuid, buffId);
    }
}
