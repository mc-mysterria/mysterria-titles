package net.mysterria.titles.api;

import net.mysterria.titles.MysterriaTitles;
import net.mysterria.titles.api.event.TitleGrantedEvent;
import net.mysterria.titles.domain.title.model.PlayerTitleData;
import net.mysterria.titles.domain.title.model.Title;
import net.mysterria.titles.integration.UnlimitedNameTagsHook;
import org.bukkit.Bukkit;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/** Bukkit service implementation of {@link TitlesService}, registered in onEnable. */
public final class BukkitTitlesService implements TitlesService {

    private final MysterriaTitles plugin;

    public BukkitTitlesService(MysterriaTitles plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean grant(UUID player, String titleId, String source) {
        if (player == null || titleId == null) return false;
        if (plugin.getTitleRegistry().get(titleId).isEmpty()) return false;

        PlayerTitleData data = loaded(player);
        if (data == null || !data.unlock(titleId)) return false;

        UnlimitedNameTagsHook.refresh(player);
        TitleGrantedEvent.fire(player, titleId, source == null || source.isBlank() ? "api" : source);
        return true;
    }

    @Override
    public boolean has(UUID player, String titleId) {
        if (player == null || titleId == null) return false;
        PlayerTitleData data = loaded(player);
        return data != null && data.hasUnlocked(titleId);
    }

    @Override
    public Set<String> titleIds() {
        Set<String> ids = new LinkedHashSet<>();
        for (Title title : plugin.getTitleRegistry().all()) {
            ids.add(title.getId());
        }
        return Collections.unmodifiableSet(ids);
    }

    private PlayerTitleData loaded(UUID player) {
        PlayerTitleData data = plugin.getPlayerDataManager().getCached(player);
        if (data != null || Bukkit.getPlayer(player) == null) return data;
        return plugin.getPlayerDataManager().load(player).join();
    }
}
