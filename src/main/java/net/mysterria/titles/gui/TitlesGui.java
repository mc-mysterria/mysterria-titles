package net.mysterria.titles.gui;

import dev.triumphteam.gui.builder.item.PaperItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import dev.ua.ikeepcalm.mysterria.audit.client.api.AuditOutcome;
import dev.ua.ikeepcalm.mysterria.audit.client.api.AuditRisk;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.mysterria.titles.MysterriaTitles;
import net.mysterria.titles.audit.AuditRow;
import net.mysterria.titles.config.PluginSettings;
import net.mysterria.titles.domain.buff.model.BonusDisplay;
import net.mysterria.titles.integration.UnlimitedNameTagsHook;
import net.mysterria.titles.domain.title.model.PlayerTitleData;
import net.mysterria.titles.domain.title.model.Title;
import net.mysterria.titles.domain.title.service.TitleRegistry;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class TitlesGui {

    private final MysterriaTitles plugin;
    private final Player player;
    private final PluginSettings settings;
    private final TitleRegistry registry;
    private final PaginatedGui gui;

    public TitlesGui(MysterriaTitles plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.settings = plugin.getConfigManager().getSettings();
        this.registry = plugin.getTitleRegistry();

        Component title = MiniMessage.miniMessage().deserialize(settings.getGuiTitle());
        this.gui = Gui.paginated()
                .title(title)
                .rows(settings.getGuiRows())
                .disableAllInteractions()
                .create();
        buildPaginated(gui);
    }

    private void buildPaginated(PaginatedGui paginatedGui) {
        int rows = settings.getGuiRows();
        int bottomRowStart = (rows - 1) * 9;

        for (int slot = bottomRowStart; slot < rows * 9; slot++) {
            paginatedGui.setItem(slot, filler());
        }

        paginatedGui.setItem(bottomRowStart + 3, PaperItemBuilder.from(Material.ARROW)
                .name(Component.text("Previous Page", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false))
                .asGuiItem(event -> paginatedGui.previous()));

        paginatedGui.setItem(bottomRowStart + 5, PaperItemBuilder.from(Material.ARROW)
                .name(Component.text("Next Page", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false))
                .asGuiItem(event -> paginatedGui.next()));

        paginatedGui.setItem(bottomRowStart + 4, clearButtonItem());

        Set<String> stored = storedUnlocked();
        String active = activeTitleId();
        boolean testMode = plugin.getTitleTestModeService().isEnabled(player.getUniqueId());

        for (Title title : registry.all()) {
            boolean unlocked = testMode || registry.isEffectivelyUnlocked(player, stored, title);
            if (!unlocked && !settings.isShowLocked()) continue;

            paginatedGui.addItem(buildTitleItem(title, unlocked, title.id().equals(active)));
        }
    }

    private Set<String> storedUnlocked() {
        PlayerTitleData data = plugin.getPlayerDataManager().getCached(player.getUniqueId());
        return data != null ? data.getUnlockedTitles() : Set.of();
    }

    private String activeTitleId() {
        PlayerTitleData data = plugin.getPlayerDataManager().getCached(player.getUniqueId());
        return data != null ? data.getActiveTitle().orElse(null) : null;
    }

    private GuiItem buildTitleItem(Title title, boolean unlocked, boolean active) {
        return unlocked ? buildUnlockedItem(title, active) : buildLockedItem(title);
    }

    private GuiItem buildUnlockedItem(Title title, boolean active) {
        List<Component> lore = new ArrayList<>();
        for (Component line : title.description()) {
            lore.add(line.decoration(TextDecoration.ITALIC, false));
        }
        lore.add(Component.empty());
        lore.add(bonusLine(title));
        lore.add(Component.empty());
        lore.add(active
                ? Component.text("Active", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false)
                : Component.text("Click to equip", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));

        return PaperItemBuilder.from(title.guiMaterial())
                .name(title.display().decoration(TextDecoration.ITALIC, false))
                .lore(lore)
                .glow(active)
                .asGuiItem(event -> equip(title));
    }

    private GuiItem buildLockedItem(Title title) {
        List<Component> lore = new ArrayList<>();
        for (Component line : title.description()) {
            lore.add(line.decoration(TextDecoration.ITALIC, false));
        }
        lore.add(Component.empty());
        lore.add(unlockHint(title));
        if (title.progressRequired() > 0) {
            lore.add(progressLine(title));
        }

        return PaperItemBuilder.from(settings.getLockedMaterial())
                .name(title.display().decoration(TextDecoration.ITALIC, false))
                .lore(lore)
                .asGuiItem();
    }

    private Component progressLine(Title title) {
        PlayerTitleData data = plugin.getPlayerDataManager().getCached(player.getUniqueId());
        int have = data != null ? data.getProgress(title.id()) : 0;
        int required = title.progressRequired();
        return Component.text("Progress: ", NamedTextColor.GRAY)
                .append(Component.text(have + "/" + required, NamedTextColor.AQUA))
                .decoration(TextDecoration.ITALIC, false);
    }

    private GuiItem clearButtonItem() {
        boolean hasActive = activeTitleId() != null;
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text(hasActive ? "Removes your active title." : "No title is currently active.", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));

        return PaperItemBuilder.from(Material.BARRIER)
                .name(Component.text("Clear Title", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false))
                .lore(lore)
                .asGuiItem(event -> unequip());
    }

    private void unequip() {
        PlayerTitleData data = plugin.getPlayerDataManager().getCached(player.getUniqueId());
        if (data == null) return;

        String previousActive = data.getActiveTitle().orElse(null);
        if (data.clearActiveTitle()) {
            player.sendMessage(Component.text("Active title cleared.", NamedTextColor.YELLOW));
            UnlimitedNameTagsHook.refresh(player.getUniqueId());
            refresh();
            auditClear(previousActive);
        }
    }

    private void auditClear(String previousActive) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("previous_active", previousActive == null ? "" : previousActive);
        metadata.put("test_mode_enabled", plugin.getTitleTestModeService().isEnabled(player.getUniqueId()));

        UUID playerId = player.getUniqueId();
        plugin.getAuditEmitter().emit(AuditOutcome.OBSERVED,
                new AuditRow("titles.title.gui_clear", AuditRisk.NORMAL, UUID.randomUUID(), previousActive,
                        playerId, playerId, "self_clear", metadata));
    }

    private GuiItem filler() {
        return PaperItemBuilder.from(settings.getFillerMaterial())
                .name(Component.empty())
                .asGuiItem();
    }

    private Component bonusLine(Title title) {
        BonusDisplay display = BonusDisplay.of(title.bonus().type());
        int percent = display.percent(title.bonus().value());
        return Component.text(display.sign() + percent + "% " + display.label(), NamedTextColor.AQUA)
                .decoration(TextDecoration.ITALIC, false);
    }

    private Component unlockHint(Title title) {
        return switch (title.unlockMethod()) {
            case PERMISSION -> Component.text("Requires permission", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false);
            case COMMAND -> Component.text("Granted by staff", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false);
            case AUTO -> Component.text("Automatically granted based on in-game progress", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false);
        };
    }

    private void equip(Title title) {
        PlayerTitleData data = plugin.getPlayerDataManager().getCached(player.getUniqueId());
        if (data == null) return;

        // Test mode only previews titles, so ownership is rechecked without its override.
        boolean stored = data.hasUnlocked(title.id());
        if (!stored && !registry.isEffectivelyUnlocked(player, data.getUnlockedTitles(), title)) {
            player.sendMessage(Component.text("You do not own this title.", NamedTextColor.YELLOW));
            refresh();
            return;
        }

        // Effectively unlocked but not stored means the player holds the title's permission.
        boolean persistedFromPermission = !stored && data.unlock(title.id());
        if (data.setActiveTitle(title.id())) {
            player.sendMessage(Component.text("Active title set to ", NamedTextColor.GREEN).append(title.display()));
            UnlimitedNameTagsHook.refresh(player.getUniqueId());
            refresh();
        }
        if (persistedFromPermission) {
            auditPermissionUnlock(title);
        }
    }

    private void auditPermissionUnlock(Title title) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("title_id", title.id());
        metadata.put("reason", "permission");
        metadata.put("permission", title.permission());
        metadata.put("unlock_method", title.unlockMethod().name());
        metadata.put("test_mode_enabled", plugin.getTitleTestModeService().isEnabled(player.getUniqueId()));

        UUID playerId = player.getUniqueId();
        plugin.getAuditEmitter().emit(AuditOutcome.OBSERVED,
                new AuditRow("titles.title.gui_persist_unlock", AuditRisk.NORMAL, UUID.randomUUID(), title.id(),
                        playerId, playerId, "permission", metadata));
    }

    private void refresh() {
        gui.clearPageItems();
        buildPaginated(gui);
        gui.update();
    }

    public void open() {
        gui.open(player);
    }
}
