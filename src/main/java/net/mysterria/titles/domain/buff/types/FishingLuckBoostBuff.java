package net.mysterria.titles.domain.buff.types;

import net.mysterria.titles.MysterriaTitles;
import net.mysterria.titles.domain.buff.model.TitleBuff;
import org.bukkit.Bukkit;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;

public class FishingLuckBoostBuff extends TitleBuff implements Listener {

    public static final String ID = "FISHING_LUCK_BOOST";

    public FishingLuckBoostBuff(MysterriaTitles plugin) {
        super(plugin);
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public void register() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void unregister() {
        HandlerList.unregisterAll(this);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onFish(PlayerFishEvent event) {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) return;
        if (!(event.getCaught() instanceof Item caughtItem)) return;

        Player player = event.getPlayer();
        double multiplier = multiplierFor(player.getUniqueId());
        if (multiplier == 1.0) return;

        ItemStack stack = caughtItem.getItemStack();
        int boosted = (int) Math.round(stack.getAmount() * multiplier);
        stack.setAmount(Math.min(stack.getMaxStackSize(), Math.max(stack.getAmount(), boosted)));
        caughtItem.setItemStack(stack);
    }
}
