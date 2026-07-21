package net.mysterria.titles.integration;

import org.alexdev.unlimitednametags.api.UNTAPI;
import org.bukkit.Bukkit;

import java.util.UUID;

public class UnlimitedNameTagsHook {

    private UnlimitedNameTagsHook() {
    }

    /**
     * UNT polls placeholders on its own interval, which lags visibly behind an in-game title
     * change. Forcing a refresh here makes the nametag update immediately instead of waiting
     * for the next poll cycle.
     */
    public static void refresh(UUID uuid) {
        if (!Bukkit.getPluginManager().isPluginEnabled("UnlimitedNameTags")) return;
        try {
            UNTAPI.getInstance().forceRefresh(uuid);
        } catch (IllegalStateException ignored) {
        }
    }
}
