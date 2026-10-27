package net.mysterria.titles.integration;

import org.alexdev.unlimitednametags.api.UNTAPI;
import org.bukkit.Bukkit;

import java.util.UUID;

public class UnlimitedNameTagsHook {

    private UnlimitedNameTagsHook() {
    }

    public static void refresh(UUID uuid) {
        if (!Bukkit.getPluginManager().isPluginEnabled("UnlimitedNameTags")) return;
        try {
            UNTAPI.getInstance().forceRefresh(uuid);
        } catch (IllegalStateException ignored) {
        }
    }
}
