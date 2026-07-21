package net.mysterria.titles.domain.title.service;

import net.mysterria.titles.MysterriaTitles;
import net.mysterria.titles.domain.title.model.PlayerTitleData;
import net.mysterria.titles.domain.title.model.Title;
import net.mysterria.titles.integration.UnlimitedNameTagsHook;
import org.bukkit.entity.Player;

/**
 * Generic progress-counter unlock: a title configured with unlock.progress-required in
 * titles.yml is granted once its stored progress counter reaches that threshold. Used for The
 * Loyalist (1 point per Advent Calendar day claimed, so the title genuinely proves every day was
 * claimed - not just the last one) but works for any future title configured the same way.
 */
public class TitleProgressService {

    private final MysterriaTitles plugin;

    public TitleProgressService(MysterriaTitles plugin) {
        this.plugin = plugin;
    }

    /**
     * Adds {@code amount} to the player's stored progress for {@code titleId} and auto-unlocks
     * the title if it reaches the configured requirement. Returns the new total (capped display
     * is the caller's job - the stored counter is never rolled back).
     */
    public int addProgress(Player player, String titleId, int amount) {
        PlayerTitleData data = plugin.getPlayerDataManager().getCached(player.getUniqueId());
        if (data == null) return 0;

        int total = data.addProgress(titleId, amount);

        if (!data.hasUnlocked(titleId)) {
            Title title = plugin.getTitleRegistry().get(titleId).orElse(null);
            if (title != null && title.progressRequired() > 0 && total >= title.progressRequired()) {
                data.unlock(titleId);
                UnlimitedNameTagsHook.refresh(player.getUniqueId());
            }
        }

        return total;
    }

    /**
     * Overwrites progress outright (does not auto-unlock) - for admin corrections.
     */
    public void setProgress(Player player, String titleId, int amount) {
        PlayerTitleData data = plugin.getPlayerDataManager().getCached(player.getUniqueId());
        if (data == null) return;
        data.setProgress(titleId, amount);
    }
}
