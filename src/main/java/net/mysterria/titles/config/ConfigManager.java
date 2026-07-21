package net.mysterria.titles.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class ConfigManager {

    private final JavaPlugin plugin;
    private final PluginSettings settings = new PluginSettings();
    private FileConfiguration titlesConfig;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        settings.load(plugin.getConfig());

        File titlesFile = new File(plugin.getDataFolder(), "titles.yml");
        if (!titlesFile.exists()) {
            plugin.saveResource("titles.yml", false);
        }
        titlesConfig = YamlConfiguration.loadConfiguration(titlesFile);
    }

    public PluginSettings getSettings() {
        return settings;
    }

    public FileConfiguration getTitlesConfig() {
        return titlesConfig;
    }
}
