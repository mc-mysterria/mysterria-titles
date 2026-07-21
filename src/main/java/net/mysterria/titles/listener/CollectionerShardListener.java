package net.mysterria.titles.listener;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.mysterria.titles.MysterriaTitles;
import net.mysterria.titles.domain.shard.CollectionerShardService;
import net.mysterria.titles.domain.title.model.PlayerTitleData;
import net.mysterria.titles.integration.UnlimitedNameTagsHook;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class CollectionerShardListener implements Listener {

    private final MysterriaTitles plugin;
    private final CollectionerShardService shardService;

    public CollectionerShardListener(MysterriaTitles plugin, CollectionerShardService shardService) {
        this.plugin = plugin;
        this.shardService = shardService;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack item = event.getItem();
        if (!shardService.isShard(item)) return;

        event.setCancelled(true);
        Player player = event.getPlayer();

        PlayerTitleData data = plugin.getPlayerDataManager().getCached(player.getUniqueId());
        if (data == null) return;

        String titleId = shardService.titleId();
        if (data.hasUnlocked(titleId)) {
            player.sendMessage(Component.text("You already hold the Collectioner title.", NamedTextColor.YELLOW));
            return;
        }

        int required = shardService.requiredCount();
        int have = shardService.count(player);

        if (have < required) {
            player.sendMessage(Component.text("Collectioner Shards: ", NamedTextColor.GRAY)
                    .append(Component.text(have + "/" + required, NamedTextColor.AQUA)));
            return;
        }

        shardService.consume(player, required);
        data.unlock(titleId);
        UnlimitedNameTagsHook.refresh(player.getUniqueId());

        player.sendMessage(Component.text("✓ ", NamedTextColor.GREEN)
                .append(Component.text("You assembled the full set and unlocked the Collectioner title!", NamedTextColor.GREEN)));
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
    }
}
