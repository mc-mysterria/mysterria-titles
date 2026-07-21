package net.mysterria.titles.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.mysterria.titles.MysterriaTitles;
import net.mysterria.titles.model.PlayerTitleData;
import net.mysterria.titles.model.Title;
import org.bukkit.OfflinePlayer;

public final class TitlesExpansion extends PlaceholderExpansion {

    private final MysterriaTitles plugin;

    public TitlesExpansion(MysterriaTitles plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "mysterria";
    }

    @Override
    public String getAuthor() {
        return "ikeepcalm";
    }

    @Override
    public String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (!params.equalsIgnoreCase("active_title")) {
            return null;
        }
        if (player == null) return "";

        PlayerTitleData data = plugin.getPlayerDataManager().getCached(player.getUniqueId());
        if (data == null) return "";

        return data.getActiveTitle()
                .flatMap(plugin.getTitleRegistry()::get)
                .map(Title::resolveTagString)
                .orElse("");
    }
}
