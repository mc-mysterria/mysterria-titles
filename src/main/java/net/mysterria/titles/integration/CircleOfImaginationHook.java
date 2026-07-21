package net.mysterria.titles.integration;

import dev.ua.ikeepcalm.coi.api.CircleOfImaginationAPI;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.Optional;

public class CircleOfImaginationHook {

    private static final String PLUGIN_NAME = "CircleOfImagination";

    private CircleOfImaginationHook() {
    }

    public static boolean isPresent() {
        return Bukkit.getPluginManager().isPluginEnabled(PLUGIN_NAME);
    }

    public static Optional<CircleOfImaginationAPI> api() {
        if (!isPresent()) return Optional.empty();

        RegisteredServiceProvider<CircleOfImaginationAPI> provider = Bukkit.getServicesManager().getRegistration(CircleOfImaginationAPI.class);
        return provider != null ? Optional.of(provider.getProvider()) : Optional.empty();
    }
}
