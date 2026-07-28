package net.mysterria.titles.domain.title.service;

import dev.ua.ikeepcalm.coi.api.CircleOfImaginationAPI;
import net.mysterria.titles.domain.storage.service.PlayerDataManager;
import net.mysterria.titles.domain.title.model.PlayerTitleData;
import net.mysterria.titles.integration.CircleOfImaginationHook;
import net.mysterria.titles.integration.UnlimitedNameTagsHook;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Auto-unlocks (never force-equips) COI-tied titles based on live Beyonder state, and revokes
 * them again if the player no longer qualifies. Thresholds are independent, not mutually
 * exclusive: a Sequence 2 Beyonder satisfies demigod (<=4), saint (<=3) AND angel (<=2)
 * simultaneously - lower sequence numbers are stronger, so clearing a harder threshold always
 * implies every easier one too.
 */
public class SequenceTitleAutoGrantService {

    private record SequenceThreshold(String titleId, int maxSequence) {
    }

    private static final List<SequenceThreshold> SEQUENCE_THRESHOLDS = List.of(
            new SequenceThreshold("demigod", 4),
            new SequenceThreshold("saint", 3),
            new SequenceThreshold("angel", 2),
            new SequenceThreshold("archangel", 1),
            new SequenceThreshold("deity", 0)
    );

    private static final String BEYONDER_TITLE_ID = "beyonder";
    private static final String UNIQUE_TITLE_ID = "unique";

    /**
     * Title ids that mirror a Circle of Imagination pathway name 1:1 (see titles.yml) - a player
     * holding that pathway at any sequence auto-unlocks (and loses it again if they drop the
     * pathway, e.g. via a pathway transfer).
     */
    private static final Set<String> PATHWAY_TITLE_IDS = Set.of(
            "door", "sun", "tyrant", "fool", "priest", "demoness", "error", "visionary",
            "fortune", "hanged", "darkness", "paragon", "sublunary", "condenser", "edict",
            "chaos", "chaosmist", "patriarch", "death", "emperor", "moon", "justiciar",
            "abyss", "giant", "mother", "hermit", "chained", "devouring", "tower", "aeon",
            "secondlaw", "everlasting"
    );

    private final PlayerDataManager playerDataManager;

    public SequenceTitleAutoGrantService(PlayerDataManager playerDataManager) {
        this.playerDataManager = playerDataManager;
    }

    public void evaluate(Player player) {
        CircleOfImaginationHook.api().ifPresent(api -> evaluate(player, api));
    }

    private void evaluate(Player player, CircleOfImaginationAPI api) {
        PlayerTitleData data = playerDataManager.getCached(player.getUniqueId());
        if (data == null) return;

        boolean isBeyonder = api.isBeyonder(player);
        int lowestSequence = isBeyonder ? api.getLowestSequence(player) : Integer.MAX_VALUE;

        boolean changed = apply(data, BEYONDER_TITLE_ID, isBeyonder);
        for (SequenceThreshold threshold : SEQUENCE_THRESHOLDS) {
            boolean eligible = isBeyonder && lowestSequence <= threshold.maxSequence();
            changed |= apply(data, threshold.titleId(), eligible);
        }

        boolean uniqueEligible = api.getUniquenessActingMultiplier(player) != 1.0;
        changed |= apply(data, UNIQUE_TITLE_ID, uniqueEligible);

        Set<String> heldPathways = isBeyonder
                ? api.getPathwayData(player).stream()
                        .map(pathway -> pathway.name().toLowerCase(Locale.ROOT))
                        .collect(Collectors.toSet())
                : Set.of();
        for (String pathwayId : PATHWAY_TITLE_IDS) {
            changed |= apply(data, pathwayId, heldPathways.contains(pathwayId));
        }

        if (changed) {
            UnlimitedNameTagsHook.refresh(player.getUniqueId());
        }
    }

    private boolean apply(PlayerTitleData data, String titleId, boolean eligible) {
        return eligible ? data.unlock(titleId) : data.revoke(titleId);
    }
}
