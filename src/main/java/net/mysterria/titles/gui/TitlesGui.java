package net.mysterria.titles.gui;

import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.mysterria.titles.MysterriaTitles;
import net.mysterria.titles.domain.buff.model.BonusDisplay;
import net.mysterria.titles.domain.title.model.PlayerTitleData;
import net.mysterria.titles.domain.title.model.Title;
import net.mysterria.titles.domain.title.model.source.TitleFilter;
import net.mysterria.titles.domain.title.service.TitleRegistry;
import net.mysterria.titles.integration.UnlimitedNameTagsHook;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import xyz.xenondevs.invui.Click;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class TitlesGui {

    private static final String GLYPH = "titles_hall";
    private static final TextColor TEXT = TextColor.fromHexString("#f0e3b8");
    private static final NamespacedKey BLANK = new NamespacedKey("titles", "gui/blank");
    private static final NamespacedKey LOCK = new NamespacedKey("titles", "gui/lock");
    private static final NamespacedKey NO_TITLE = new NamespacedKey("titles", "gui/no_title");
    private static final NamespacedKey TOOLTIP = new NamespacedKey("titles", "hall");
    private static final int PROGRESS_SEGMENTS = 10;

    private final MysterriaTitles plugin;
    private final Player player;
    private final TitleRegistry registry;
    private final PlayerTitleData data;
    private final Menu menu;
    private TitleFilter filter = TitleFilter.ALL;
    private int page = 1;
    private boolean testMode;
    private Set<String> stored = Set.of();

    public TitlesGui(MysterriaTitles plugin, Player player, PlayerTitleData data) {
        this.plugin = plugin;
        this.player = player;
        this.registry = plugin.getTitleRegistry();
        this.data = data;
        this.menu = new Menu(plugin, player, Layout.ROWS, title());
        render();
    }

    public void open() {
        menu.open();
    }

    private void render() {
        testMode = plugin.getTitleTestModeService().isEnabled(player.getUniqueId());
        stored = data.getUnlockedTitles();
        List<Title> shown = shownTitles();
        int pages = Math.max(1, (shown.size() + Layout.LOW - 1) / Layout.LOW);
        page = Math.min(page, pages);
        for (int i = 0; i < Layout.LOW; i++) {
            int index = (page - 1) * Layout.LOW + i;
            if (index < shown.size()) {
                Title title = shown.get(index);
                menu.set(i, titleItem(title), click -> select(title));
            } else {
                menu.clear(i);
            }
        }
        renderCollection();
        renderEquipped();
        renderFilter();
        renderDeck(pages);
    }

    private List<Title> shownTitles() {
        return registry.all().stream()
                .filter(title -> isUnlocked(title) || plugin.getConfigManager().getSettings().isShowLocked())
                .filter(title -> filter.accepts(isUnlocked(title)))
                .sorted(Comparator.comparing(title -> !isUnlocked(title)))
                .toList();
    }

    private ItemStack titleItem(Title title) {
        List<Component> lore = new ArrayList<>(title.getDescription());
        lore.add(Component.empty());
        if (!isUnlocked(title)) {
            lore.add(line("When equipped: " + bonus(title), NamedTextColor.DARK_AQUA));
            lore.add(Component.empty());
            lore.add(line("Locked", NamedTextColor.RED).append(line(" · " + title.getUnlockMethod().getHint(), NamedTextColor.GRAY)));
            if (title.getProgressRequired() > 0) {
                lore.add(progress(title));
            }
            return item(Material.GRAY_DYE, LOCK, title.getDisplay(), lore);
        }
        lore.add(line(bonus(title), NamedTextColor.AQUA));
        lore.add(Component.empty());
        if (isEquipped(title)) {
            lore.add(line("Equipped", NamedTextColor.GREEN));
            lore.add(line("Click to unequip", NamedTextColor.YELLOW));
        } else {
            lore.add(line("Click to equip", NamedTextColor.YELLOW));
        }
        ItemStack item = item(title.getGuiMaterial(), null, title.getDisplay(), lore);
        item.setData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, isEquipped(title));
        return item;
    }

    private void renderCollection() {
        long owned = registry.all().stream().filter(this::isUnlocked).count();
        menu.set(Layout.COLLECTION, item(Material.BOOK, BLANK, line("Your Collection", NamedTextColor.GOLD), List.of(
                line("Owned " + owned + " of " + registry.all().size() + " titles", NamedTextColor.AQUA),
                Component.empty(),
                line("Only your equipped title's bonus is active.", NamedTextColor.GRAY),
                line("Pathway and Sequence titles follow your", NamedTextColor.GRAY),
                line("Beyonder state and leave if you lose it.", NamedTextColor.GRAY)
        )), click -> Menu.click(player));
    }

    private void renderEquipped() {
        Title equipped = data.getActiveTitle().flatMap(registry::get).orElse(null);
        ItemStack shown = equipped != null ? titleItem(equipped)
                : item(Material.NAME_TAG, NO_TITLE, line("No title equipped", NamedTextColor.GRAY),
                        List.of(line("Pick one from the shelf above.", NamedTextColor.DARK_GRAY)));
        Consumer<Click> click = equipped != null ? event -> unequip() : event -> Menu.deny(player);
        menu.set(Layout.EQUIPPED, ghost(shown), click);
        menu.set(Layout.EQUIPPED_PEDESTAL, shown, click);
    }

    private void renderFilter() {
        List<Component> lore = new ArrayList<>();
        for (TitleFilter option : TitleFilter.values()) {
            lore.add(option == filter
                    ? line("▶ " + option.getLabel(), NamedTextColor.AQUA)
                    : line("  " + option.getLabel(), NamedTextColor.DARK_GRAY));
        }
        lore.add(Component.empty());
        lore.add(line("Click to switch", NamedTextColor.YELLOW));
        menu.set(Layout.FILTER, item(Material.SPYGLASS, BLANK, line("Show: " + filter.getLabel(), NamedTextColor.GOLD), lore), click -> {
            Menu.click(player);
            filter = filter.next();
            page = 1;
            render();
        });
    }

    private void renderDeck(int pages) {
        Component pageLine = line("Page " + page + " of " + pages, NamedTextColor.GRAY);
        menu.set(Layout.PREVIOUS, item(Material.ARROW, BLANK, line("Previous page", NamedTextColor.YELLOW), List.of(pageLine)),
                click -> turn(page > 1, page - 1));
        menu.set(Layout.NEXT, item(Material.ARROW, BLANK, line("Next page", NamedTextColor.YELLOW), List.of(pageLine)),
                click -> turn(page < pages, page + 1));
        menu.set(Layout.CLOSE, item(Material.BARRIER, BLANK, line("Close", NamedTextColor.RED), List.of()), click -> {
            Menu.click(player);
            menu.close();
        });
    }

    private void turn(boolean possible, int target) {
        if (!possible) {
            Menu.deny(player);
            return;
        }
        Menu.click(player);
        page = target;
        render();
    }

    private void select(Title title) {
        if (!isUnlocked(title)) {
            Menu.deny(player);
            player.sendActionBar(line(title.getUnlockMethod().getHint(), NamedTextColor.RED));
            return;
        }
        if (isEquipped(title)) {
            unequip();
            return;
        }
        if (!isOwned(title)) {
            Menu.deny(player);
            player.sendActionBar(line("Test mode only previews this title; grant it to wear it", NamedTextColor.YELLOW));
            return;
        }
        data.unlock(title.getId());
        data.setActiveTitle(title.getId());
        Menu.success(player);
        player.sendActionBar(line("Equipped ", NamedTextColor.GREEN).append(title.getDisplay()));
        UnlimitedNameTagsHook.refresh(player.getUniqueId());
        render();
    }

    private void unequip() {
        if (!data.clearActiveTitle()) {
            Menu.deny(player);
            return;
        }
        Menu.click(player);
        player.sendActionBar(line("Title unequipped", NamedTextColor.YELLOW));
        UnlimitedNameTagsHook.refresh(player.getUniqueId());
        render();
    }

    private boolean isUnlocked(Title title) {
        return testMode || isOwned(title);
    }

    private boolean isOwned(Title title) {
        return registry.isEffectivelyUnlocked(player, stored, title);
    }

    private boolean isEquipped(Title title) {
        return data.getActiveTitle().filter(title.getId()::equals).isPresent();
    }

    private String bonus(Title title) {
        return BonusDisplay.of(title.getBonus().getType()).describe(title.getBonus().getValue());
    }

    private Component progress(Title title) {
        int required = title.getProgressRequired();
        int have = Math.clamp(data.getProgress(title.getId()), 0, required);
        int filled = have * PROGRESS_SEGMENTS / required;
        return line("■".repeat(filled), NamedTextColor.AQUA)
                .append(line("■".repeat(PROGRESS_SEGMENTS - filled), NamedTextColor.DARK_GRAY))
                .append(line(" " + have + "/" + required, NamedTextColor.GRAY));
    }

    private Component title() {
        Component text = Component.text("Titles", TEXT);
        if (!plugin.getServer().getPluginManager().isPluginEnabled("Nexo")) {
            return text;
        }
        return MiniMessage.miniMessage().deserialize("<shift:-15><glyph:" + GLYPH + "><shift:-176>").append(text);
    }

    private static ItemStack item(Material material, NamespacedKey model, Component name, List<Component> lore) {
        ItemStack item = new ItemStack(material);
        item.editMeta(meta -> {
            meta.displayName(name.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            meta.lore(lore.stream().map(line -> line.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)).toList());
            meta.setTooltipStyle(TOOLTIP);
            if (model != null) {
                meta.setItemModel(model);
            }
        });
        return item;
    }

    private static ItemStack ghost(ItemStack item) {
        ItemStack ghost = item.clone();
        ghost.editMeta(meta -> meta.setItemModel(BLANK));
        return ghost;
    }

    private static Component line(String text, NamedTextColor color) {
        return Component.text(text, color);
    }
}
