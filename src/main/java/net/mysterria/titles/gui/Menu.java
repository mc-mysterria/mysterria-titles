package net.mysterria.titles.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import xyz.xenondevs.invui.Click;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.window.Window;

import java.util.function.Consumer;

public class Menu {

    private final Plugin plugin;
    private final Gui upper;
    private final Gui lower;
    private final Window window;
    private boolean clickPending;

    public Menu(Plugin plugin, Player player, int rows, Component title) {
        this.plugin = plugin;
        this.upper = Gui.empty(9, rows);
        this.lower = Gui.empty(9, 4);
        this.window = Window.builder().setViewer(player).setUpperGui(upper).setLowerGui(lower).setTitle(title).build();
    }

    public void set(int slot, ItemStack stack, Consumer<Click> click) {
        put(slot, clickable(stack, click));
    }

    public void set(int[] slots, ItemStack stack, Consumer<Click> click) {
        Item item = clickable(stack, click);
        for (int slot : slots) {
            put(slot, item);
        }
    }

    private Item clickable(ItemStack stack, Consumer<Click> click) {
        return Item.builder().setItemProvider(stack).addClickHandler(event -> {
            if (clickPending) {
                return;
            }
            clickPending = true;
            event.player().getScheduler().run(plugin, task -> {
                clickPending = false;
                if (window.isOpen()) {
                    click.accept(event);
                }
            }, null);
        }).build();
    }

    public void clear(int slot) {
        put(slot, Item.EMPTY);
    }

    public void open() {
        window.open();
    }

    public void close() {
        window.close();
    }

    public static void click(Player player) {
        player.playSound(player, Sound.UI_BUTTON_CLICK, 0.6f, 1.0f);
    }

    public static void success(Player player) {
        player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.6f, 1.2f);
    }

    public static void deny(Player player) {
        player.playSound(player, Sound.ENTITY_VILLAGER_NO, 0.6f, 1.0f);
    }

    private void put(int slot, Item item) {
        int upperSize = upper.getSize();
        if (slot < upperSize) {
            upper.setItem(slot, item);
        } else {
            lower.setItem(slot - upperSize, item);
        }
    }
}
