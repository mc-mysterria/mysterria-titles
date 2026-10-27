package net.mysterria.titles.domain.buff.service;

import lombok.RequiredArgsConstructor;
import net.mysterria.titles.domain.storage.service.PlayerDataManager;
import net.mysterria.titles.domain.title.model.PlayerTitleData;
import net.mysterria.titles.domain.title.model.Title;
import net.mysterria.titles.domain.title.service.TitleRegistry;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class TitleBonusService {

    private final PlayerDataManager playerDataManager;
    private final TitleRegistry titleRegistry;

    public Optional<Title> getActiveTitle(UUID uuid) {
        PlayerTitleData data = playerDataManager.getCached(uuid);
        if (data == null) return Optional.empty();
        return data.getActiveTitle().flatMap(titleRegistry::get);
    }

    public double getMultiplier(UUID uuid, String buffId) {
        return getActiveTitle(uuid)
                .filter(title -> title.getBonus().getType().equals(buffId))
                .map(title -> 1.0 + title.getBonus().getValue())
                .orElse(1.0);
    }

    public double apply(UUID uuid, String buffId, double base) {
        return base * getMultiplier(uuid, buffId);
    }
}
