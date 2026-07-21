package net.mysterria.titles.domain.shard;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.mysterria.titles.MysterriaTitles;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mints and redeems the Collectioner Shard - a festive-shop-only item. Players buy shards a few
 * at a time and must assemble the full configured set before the title unlocks, deliberately
 * making Collectioner the hardest title to obtain (see anniversary event plan).
 */
public class CollectionerShardService {

    private static final NamespacedKey SHARD_KEY = new NamespacedKey("mysterria_titles", "collectioner_shard");
    private static final String COLLECTIONER_TITLE_ID = "collectioner";

    private final MysterriaTitles plugin;

    private int requiredCount;
    private Material material;
    private String nameRaw;
    private List<String> loreRaw;

    public CollectionerShardService(MysterriaTitles plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        var section = plugin.getConfigManager().getTitlesConfig().getConfigurationSection("collectioner-shard");

        requiredCount = section != null ? section.getInt("required-count", 5) : 5;

        String materialName = section != null ? section.getString("material", "AMETHYST_CLUSTER") : "AMETHYST_CLUSTER";
        Material parsed = Material.matchMaterial(materialName == null ? "" : materialName);
        material = parsed != null ? parsed : Material.AMETHYST_CLUSTER;

        nameRaw = section != null ? section.getString("name", "<#CC8844>Collectioner Shard") : "<#CC8844>Collectioner Shard";
        loreRaw = section != null ? section.getStringList("lore") : List.of();
    }

    public int requiredCount() {
        return requiredCount;
    }

    public NamespacedKey shardKey() {
        return SHARD_KEY;
    }

    public String titleId() {
        return COLLECTIONER_TITLE_ID;
    }

    public ItemStack create(int amount) {
        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();

        MiniMessage miniMessage = MiniMessage.miniMessage();
        meta.displayName(miniMessage.deserialize(nameRaw).decoration(TextDecoration.ITALIC, false));

        List<Component> lore = loreRaw.stream()
                .map(miniMessage::deserialize)
                .map(line -> line.decoration(TextDecoration.ITALIC, false))
                .collect(Collectors.toList());
        meta.lore(lore);

        meta.getPersistentDataContainer().set(SHARD_KEY, PersistentDataType.STRING, "1");

        item.setItemMeta(meta);
        return item;
    }

    public boolean isShard(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(SHARD_KEY, PersistentDataType.STRING);
    }

    public int count(Player player) {
        PlayerInventory inventory = player.getInventory();
        int total = 0;
        for (ItemStack stack : inventory.getContents()) {
            if (isShard(stack)) {
                total += stack.getAmount();
            }
        }
        return total;
    }

    /**
     * Removes exactly {@code amount} shards from across the player's inventory. Only call after
     * confirming {@link #count(Player)} >= amount.
     */
    public void consume(Player player, int amount) {
        PlayerInventory inventory = player.getInventory();
        int remaining = amount;

        ItemStack[] contents = inventory.getContents();
        for (int slot = 0; slot < contents.length && remaining > 0; slot++) {
            ItemStack stack = contents[slot];
            if (!isShard(stack)) continue;

            int take = Math.min(remaining, stack.getAmount());
            stack.setAmount(stack.getAmount() - take);
            remaining -= take;

            if (stack.getAmount() <= 0) {
                inventory.setItem(slot, null);
            } else {
                inventory.setItem(slot, stack);
            }
        }
    }
}
