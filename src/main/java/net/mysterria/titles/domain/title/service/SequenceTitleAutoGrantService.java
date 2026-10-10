package net.mysterria.titles.domain.title.service;

import dev.ua.ikeepcalm.coi.api.CircleOfImaginationAPI;
import dev.ua.ikeepcalm.mysterria.audit.client.api.AuditOutcome;
import dev.ua.ikeepcalm.mysterria.audit.client.api.AuditRisk;
import net.mysterria.titles.audit.AuditRow;
import net.mysterria.titles.audit.TitlesAuditEmitter;
import net.mysterria.titles.domain.storage.service.PlayerDataManager;
import net.mysterria.titles.domain.title.model.PlayerTitleData;
import net.mysterria.titles.integration.CircleOfImaginationHook;
import net.mysterria.titles.integration.UnlimitedNameTagsHook;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
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

    public record Trigger(String source, String pathway, Integer oldSequence, Integer newSequence) {
        public static final Trigger RECHECK = new Trigger("recheck", null, null, null);
    }

    private record Changes(List<String> granted, List<String> revoked) {
        boolean any() {
            return !granted.isEmpty() || !revoked.isEmpty();
        }
    }

    private final PlayerDataManager playerDataManager;
    private final TitlesAuditEmitter auditEmitter;

    public SequenceTitleAutoGrantService(PlayerDataManager playerDataManager, TitlesAuditEmitter auditEmitter) {
        this.playerDataManager = playerDataManager;
        this.auditEmitter = auditEmitter;
    }

    public void evaluate(Player player) {
        evaluate(player, Trigger.RECHECK);
    }

    public void evaluate(Player player, Trigger trigger) {
        CircleOfImaginationHook.api().ifPresent(api -> evaluate(player, api, trigger));
    }

    private void evaluate(Player player, CircleOfImaginationAPI api, Trigger trigger) {
        PlayerTitleData data = playerDataManager.getCached(player.getUniqueId());
        if (data == null) return;

        boolean isBeyonder = api.isBeyonder(player);
        int lowestSequence = isBeyonder ? api.getLowestSequence(player) : Integer.MAX_VALUE;
        Changes changes = new Changes(new ArrayList<>(), new ArrayList<>());

        apply(data, BEYONDER_TITLE_ID, isBeyonder, changes);
        for (SequenceThreshold threshold : SEQUENCE_THRESHOLDS) {
            boolean eligible = isBeyonder && lowestSequence <= threshold.maxSequence();
            apply(data, threshold.titleId(), eligible, changes);
        }

        double uniquenessMultiplier = api.getUniquenessActingMultiplier(player);
        apply(data, UNIQUE_TITLE_ID, uniquenessMultiplier != 1.0, changes);

        Set<String> heldPathways = isBeyonder
                ? api.getPathwayData(player).stream()
                        .map(pathway -> pathway.name().toLowerCase(Locale.ROOT))
                        .collect(Collectors.toSet())
                : Set.of();
        for (String pathwayId : PATHWAY_TITLE_IDS) {
            apply(data, pathwayId, heldPathways.contains(pathwayId), changes);
        }

        if (changes.any()) {
            UnlimitedNameTagsHook.refresh(player.getUniqueId());
            Map<String, Object> metadata = auditMetadata(changes, trigger, isBeyonder, lowestSequence,
                    heldPathways, uniquenessMultiplier);
            UUID playerId = player.getUniqueId();
            auditEmitter.emit(AuditOutcome.OBSERVED,
                    new AuditRow("titles.title.auto_change", AuditRisk.NORMAL, UUID.randomUUID(),
                            playerId.toString(), null, playerId, null, metadata));
        }
    }

    private static Map<String, Object> auditMetadata(Changes changes, Trigger trigger, boolean isBeyonder,
                                                     int lowestSequence, Set<String> heldPathways,
                                                     double uniquenessMultiplier) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("granted", String.join(",", changes.granted()));
        metadata.put("revoked", String.join(",", changes.revoked()));
        metadata.put("granted_count", changes.granted().size());
        metadata.put("revoked_count", changes.revoked().size());
        metadata.put("is_beyonder", isBeyonder);
        if (isBeyonder) {
            metadata.put("lowest_sequence", lowestSequence);
        }
        metadata.put("pathways", heldPathways.stream().sorted().collect(Collectors.joining(",")));
        metadata.put("uniqueness_multiplier", uniquenessMultiplier);
        metadata.put("trigger", trigger.source());
        if (trigger.pathway() != null) {
            metadata.put("trigger_pathway", trigger.pathway());
        }
        if (trigger.oldSequence() != null) {
            metadata.put("trigger_old_sequence", trigger.oldSequence());
        }
        if (trigger.newSequence() != null) {
            metadata.put("trigger_new_sequence", trigger.newSequence());
        }
        return metadata;
    }

    private static void apply(PlayerTitleData data, String titleId, boolean eligible, Changes changes) {
        if (eligible) {
            if (data.unlock(titleId)) changes.granted().add(titleId);
        } else if (data.revoke(titleId)) {
            changes.revoked().add(titleId);
        }
    }
}
