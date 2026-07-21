package net.mysterria.titles.domain.buff.types;

import net.mysterria.titles.MysterriaTitles;
import net.mysterria.titles.domain.buff.model.TitleBuff;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

public class MobLootBoostBuff extends TitleBuff implements Listener {

    public static final String ID = "MOB_LOOT_BOOST";

    public MobLootBoostBuff(MysterriaTitles plugin) {
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
    public void onDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;

        double multiplier = multiplierFor(killer.getUniqueId());
        if (multiplier == 1.0) return;

        for (ItemStack drop : event.getDrops()) {
            int boosted = (int) Math.round(drop.getAmount() * multiplier);
            drop.setAmount(Math.min(drop.getMaxStackSize(), Math.max(drop.getAmount(), boosted)));
        }
    }
}
