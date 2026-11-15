package net.mysterria.titles.integration;

import dev.ua.ikeepcalm.coi.api.CircleOfImaginationApi;
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

    public static Optional<CircleOfImaginationApi> api() {
        if (!isPresent()) return Optional.empty();

        RegisteredServiceProvider<CircleOfImaginationApi> provider = Bukkit.getServicesManager().getRegistration(CircleOfImaginationApi.class);
        return provider != null ? Optional.of(provider.getProvider()) : Optional.empty();
    }
}
