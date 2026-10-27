package net.mysterria.titles.config.models;

import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;

@Getter
public class PluginSettings {

    private long autosaveIntervalSeconds;
    private boolean flushOnQuit;
    private boolean showLocked;

    public void load(FileConfiguration config) {
        autosaveIntervalSeconds = config.getLong("storage.autosave-interval-seconds");
        flushOnQuit = config.getBoolean("storage.flush-on-quit");
        showLocked = config.getBoolean("gui.show-locked");
    }
}
