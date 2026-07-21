package net.mysterria.titles.domain.title.service;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.mysterria.titles.domain.title.model.source.RenderType;
import net.mysterria.titles.domain.title.model.Title;
import net.mysterria.titles.domain.title.model.TitleBonus;
import net.mysterria.titles.domain.title.model.source.UnlockMethod;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public final class TitleRegistry {

    private final Logger logger;
    private final Map<String, Title> titles = new LinkedHashMap<>();

    public TitleRegistry(Logger logger) {
        this.logger = logger;
    }

    public void load(FileConfiguration titlesConfig) {
        titles.clear();

        ConfigurationSection root = titlesConfig.getConfigurationSection("titles");
        if (root == null) {
            logger.warning("titles.yml has no 'titles' section; no titles loaded.");
            return;
        }

        MiniMessage miniMessage = MiniMessage.miniMessage();

        for (String id : root.getKeys(false)) {
            ConfigurationSection section = root.getConfigurationSection(id);
            if (section == null) continue;

            try {
                Title title = parse(id, section, miniMessage);
                titles.put(title.id(), title);
            } catch (IllegalArgumentException ex) {
                logger.warning("Skipping invalid title '" + id + "': " + ex.getMessage());
            }
        }
    }

    private Title parse(String id, ConfigurationSection section, MiniMessage miniMessage) {
        Component display = miniMessage.deserialize(section.getString("display", ""));

        List<Component> description = section.getStringList("description").stream()
                .map(miniMessage::deserialize)
                .collect(Collectors.toList());

        RenderType renderType;
        try {
            renderType = RenderType.valueOf(section.getString("render-type", "").toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("invalid or missing render-type");
        }

        String nexoTexture = section.getString("nexo-texture", "");

        if (renderType == RenderType.STATIC && nexoTexture.isEmpty()) {
            throw new IllegalArgumentException("render-type STATIC requires a non-empty nexo-texture");
        }

        String animationFrameBase = "";
        int animationFrameCount = 0;
        long animationFrameDurationMs = 0;
        if (renderType == RenderType.ANIMATED) {
            ConfigurationSection animationSection = section.getConfigurationSection("animation");
            if (animationSection == null) {
                throw new IllegalArgumentException("render-type ANIMATED requires an 'animation' section");
            }
            animationFrameBase = animationSection.getString("frame-base", "");
            animationFrameCount = animationSection.getInt("frame-count", 0);
            animationFrameDurationMs = animationSection.getLong("frame-duration-ms", 0);
            if (animationFrameBase.isEmpty()) {
                throw new IllegalArgumentException("animation.frame-base must not be empty");
            }
            if (animationFrameCount <= 0) {
                throw new IllegalArgumentException("animation.frame-count must be > 0");
            }
            if (animationFrameDurationMs <= 0) {
                throw new IllegalArgumentException("animation.frame-duration-ms must be > 0");
            }
        }

        ConfigurationSection bonusSection = section.getConfigurationSection("bonus");
        if (bonusSection == null) {
            throw new IllegalArgumentException("missing 'bonus' section");
        }
        String bonusType = bonusSection.getString("type", "");
        double bonusValue = bonusSection.getDouble("value", -1);
        if (bonusType.isEmpty()) {
            throw new IllegalArgumentException("bonus.type must not be empty");
        }
        if (!Double.isFinite(bonusValue) || bonusValue < 0) {
            throw new IllegalArgumentException("bonus.value must be finite and >= 0");
        }
        TitleBonus bonus = new TitleBonus(bonusType, bonusValue);

        ConfigurationSection guiSection = section.getConfigurationSection("gui");
        Material guiMaterial = Material.PAPER;
        int guiSlot = 0;
        if (guiSection != null) {
            Material parsed = Material.matchMaterial(guiSection.getString("material", "PAPER"));
            guiMaterial = parsed != null ? parsed : Material.PAPER;
            guiSlot = guiSection.getInt("slot", 0);
        }

        ConfigurationSection unlockSection = section.getConfigurationSection("unlock");
        UnlockMethod unlockMethod = UnlockMethod.COMMAND;
        String permission = "";
        if (unlockSection != null) {
            String methodRaw = unlockSection.getString("method", "COMMAND");
            try {
                unlockMethod = UnlockMethod.valueOf(methodRaw.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("invalid unlock.method '" + methodRaw + "'");
            }
            permission = unlockSection.getString("permission", "");
        }
        if (unlockMethod == UnlockMethod.PERMISSION && permission.isEmpty()) {
            throw new IllegalArgumentException("unlock.method PERMISSION requires a non-empty permission");
        }

        return new Title(id, display, description, renderType, nexoTexture,
                animationFrameBase, animationFrameCount, animationFrameDurationMs,
                bonus, guiMaterial, guiSlot, unlockMethod, permission);
    }

    public Optional<Title> get(String id) {
        return Optional.ofNullable(titles.get(id));
    }

    public Collection<Title> all() {
        return Collections.unmodifiableCollection(titles.values());
    }

    public boolean exists(String id) {
        return titles.containsKey(id);
    }

    public int size() {
        return titles.size();
    }

    /**
     * Effective unlock per §7.1: stored unlocked ∪ {PERMISSION titles held} ∪ {AUTO titles}.
     */
    public boolean isEffectivelyUnlocked(Player player, Set<String> storedUnlocked, Title title) {
        if (storedUnlocked.contains(title.id())) return true;
        if (title.unlockMethod() == UnlockMethod.AUTO) return true;
        if (title.unlockMethod() == UnlockMethod.PERMISSION) {
            return !title.permission().isEmpty() && player.hasPermission(title.permission());
        }
        return false;
    }
}
