package net.mysterria.titles.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.mysterria.titles.config.models.PluginSettings;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

@Getter
@RequiredArgsConstructor
public class ConfigManager {

    private final JavaPlugin plugin;
    private final PluginSettings settings = new PluginSettings();
    private FileConfiguration titlesConfig;

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
}
