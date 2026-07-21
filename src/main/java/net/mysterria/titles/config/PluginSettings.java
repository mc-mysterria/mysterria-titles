package net.mysterria.titles.config;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

public class PluginSettings {

    private long autosaveIntervalSeconds;
    private boolean flushOnQuit;

    private String guiTitle;
    private int guiRows;
    private Material fillerMaterial;
    private Material lockedMaterial;
    private boolean showLocked;

    private boolean debug;

    public void load(FileConfiguration config) {
        autosaveIntervalSeconds = config.getLong("storage.autosave-interval-seconds", 300);
        flushOnQuit = config.getBoolean("storage.flush-on-quit", true);

        guiTitle = config.getString("gui.title", "<gradient:#7b2ff7:#f107a3>Titles</gradient>");
        guiRows = config.getInt("gui.rows", 6);
        fillerMaterial = parseMaterial(config.getString("gui.filler-material", "GRAY_STAINED_GLASS_PANE"), Material.GRAY_STAINED_GLASS_PANE);
        lockedMaterial = parseMaterial(config.getString("gui.locked-material", "BARRIER"), Material.BARRIER);
        showLocked = config.getBoolean("gui.show-locked", true);

        debug = config.getBoolean("debug", false);
    }

    private Material parseMaterial(String name, Material fallback) {
        Material material = Material.matchMaterial(name == null ? "" : name);
        return material != null ? material : fallback;
    }

    public long getAutosaveIntervalSeconds() {
        return autosaveIntervalSeconds;
    }

    public boolean isFlushOnQuit() {
        return flushOnQuit;
    }

    public String getGuiTitle() {
        return guiTitle;
    }

    public int getGuiRows() {
        return guiRows;
    }

    public Material getFillerMaterial() {
        return fillerMaterial;
    }

    public Material getLockedMaterial() {
        return lockedMaterial;
    }

    public boolean isShowLocked() {
        return showLocked;
    }

    public boolean isDebug() {
        return debug;
    }
}
