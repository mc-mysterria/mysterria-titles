package net.mysterria.titles.domain.title.service;

import lombok.RequiredArgsConstructor;
import net.mysterria.titles.MysterriaTitles;
import net.mysterria.titles.domain.title.model.PlayerTitleData;
import net.mysterria.titles.domain.title.model.Title;
import net.mysterria.titles.integration.UnlimitedNameTagsHook;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class TitleProgressService {

    private final MysterriaTitles plugin;

    public int addProgress(Player player, String titleId, int amount) {
        PlayerTitleData data = plugin.getPlayerDataManager().getCached(player.getUniqueId());
        if (data == null) return 0;

        int total = data.addProgress(titleId, amount);

        if (!data.hasUnlocked(titleId)) {
            Title title = plugin.getTitleRegistry().get(titleId).orElse(null);
            if (title != null && title.getProgressRequired() > 0 && total >= title.getProgressRequired()) {
                data.unlock(titleId);
                UnlimitedNameTagsHook.refresh(player.getUniqueId());
            }
        }

        return total;
    }

    public void setProgress(Player player, String titleId, int amount) {
        PlayerTitleData data = plugin.getPlayerDataManager().getCached(player.getUniqueId());
        if (data == null) return;
        data.setProgress(titleId, amount);
    }
}
