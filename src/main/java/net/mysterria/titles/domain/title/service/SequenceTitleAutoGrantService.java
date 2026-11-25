package net.mysterria.titles.domain.title.service;

import dev.ua.ikeepcalm.coi.api.CircleOfImaginationApi;
import dev.ua.ikeepcalm.coi.api.model.BeyonderView;
import lombok.RequiredArgsConstructor;
import net.mysterria.titles.api.event.TitleGrantedEvent;
import net.mysterria.titles.domain.storage.service.PlayerDataManager;
import net.mysterria.titles.domain.title.model.PlayerTitleData;
import net.mysterria.titles.integration.CircleOfImaginationHook;
import net.mysterria.titles.integration.UnlimitedNameTagsHook;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class SequenceTitleAutoGrantService {

    private static final Map<String, Integer> SEQUENCE_THRESHOLDS = Map.of(
            "demigod", 4,
            "saint", 3,
            "angel", 2,
            "archangel", 1,
            "deity", 0
    );

    private static final String BEYONDER_TITLE_ID = "beyonder";
    private static final String UNIQUE_TITLE_ID = "unique";

    private static final Set<String> PATHWAY_TITLE_IDS = Set.of(
            "door", "sun", "tyrant", "fool", "priest", "demoness", "error", "visionary",
            "fortune", "hanged", "darkness", "paragon", "sublunary", "condenser", "edict",
            "chaos", "chaosmist", "patriarch", "death", "emperor", "moon", "justiciar",
            "abyss", "giant", "mother", "hermit", "chained", "devouring", "tower", "aeon",
            "secondlaw", "everlasting"
    );

    private final PlayerDataManager playerDataManager;

    public void evaluate(Player player) {
        CircleOfImaginationHook.api().ifPresent(api -> evaluate(player, api));
    }

    private void evaluate(Player player, CircleOfImaginationApi api) {
        PlayerTitleData data = playerDataManager.getCached(player.getUniqueId());
        if (data == null) return;

        Optional<BeyonderView> beyonder = api.beyonders().find(player.getUniqueId());
        boolean isBeyonder = beyonder.isPresent();
        int lowestSequence = beyonder.map(BeyonderView::getLowestSequence).orElse(Integer.MAX_VALUE);

        boolean changed = apply(data, BEYONDER_TITLE_ID, isBeyonder);
        for (Map.Entry<String, Integer> threshold : SEQUENCE_THRESHOLDS.entrySet()) {
            changed |= apply(data, threshold.getKey(), isBeyonder && lowestSequence <= threshold.getValue());
        }

        // shortcut: CoI API 1.6.1 dropped the uniqueness multiplier, so the unique title is left as-is (not granted, not revoked); restore once the API exposes it again.

        Set<String> heldPathways = beyonder.map(view -> view.getOwnedPathways().stream()
                        .map(pathway -> pathway.getKey().toLowerCase(Locale.ROOT))
                        .collect(Collectors.toSet()))
                .orElse(Set.of());
        for (String pathwayId : PATHWAY_TITLE_IDS) {
            changed |= apply(data, pathwayId, heldPathways.contains(pathwayId));
        }

        if (changed) {
            UnlimitedNameTagsHook.refresh(player.getUniqueId());
        }
    }

    private boolean apply(PlayerTitleData data, String titleId, boolean eligible) {
        if (!eligible) return data.revoke(titleId);

        boolean granted = data.unlock(titleId);
        if (granted) {
            TitleGrantedEvent.fire(data.getUuid(), titleId, "sequence");
        }
        return granted;
    }
}
