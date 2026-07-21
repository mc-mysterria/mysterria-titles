package net.mysterria.titles.domain.buff.service;

import net.mysterria.titles.domain.title.model.PlayerTitleData;
import net.mysterria.titles.domain.title.model.Title;
import net.mysterria.titles.domain.title.service.TitleRegistry;
import net.mysterria.titles.domain.storage.service.PlayerDataManager;

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
