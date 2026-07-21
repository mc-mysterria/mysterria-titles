package net.mysterria.titles.domain.shard;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.mysterria.titles.MysterriaTitles;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mints the Anniversary Token - the festive shop's currency for the 1-year anniversary event
 * (22.07.2026-05.08.2026). Minted only here so every source (Advent Calendar rewards) produces
 * an item structurally identical to the festive shop's configured payment-item, which matches
 * on material + full item meta (see BrilliantEmporium seasonal-shops/anniversary_2026.yml).
 */
public class AnniversaryTokenService {

    private static final NamespacedKey TOKEN_KEY = new NamespacedKey("mysterria_titles", "anniversary_token");

    private final MysterriaTitles plugin;

    private Material material;
    private String nameRaw;
    private List<String> loreRaw;

    public AnniversaryTokenService(MysterriaTitles plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        var section = plugin.getConfigManager().getTitlesConfig().getConfigurationSection("anniversary-token");

        String materialName = section != null ? section.getString("material", "AMETHYST_SHARD") : "AMETHYST_SHARD";
        Material parsed = Material.matchMaterial(materialName == null ? "" : materialName);
        material = parsed != null ? parsed : Material.AMETHYST_SHARD;

        nameRaw = section != null ? section.getString("name", "<gold><bold>Anniversary Token") : "<gold><bold>Anniversary Token";
        loreRaw = section != null ? section.getStringList("lore") : List.of();
    }

    public NamespacedKey tokenKey() {
        return TOKEN_KEY;
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

        meta.getPersistentDataContainer().set(TOKEN_KEY, PersistentDataType.STRING, "1");

        item.setItemMeta(meta);
        return item;
    }

    public boolean isToken(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(TOKEN_KEY, PersistentDataType.STRING);
    }
}
