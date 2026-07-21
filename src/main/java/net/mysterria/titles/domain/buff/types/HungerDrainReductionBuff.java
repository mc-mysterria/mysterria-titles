package net.mysterria.titles.domain.buff.types;

import net.mysterria.titles.MysterriaTitles;
import net.mysterria.titles.domain.buff.model.TitleBuff;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public class HungerDrainReductionBuff extends TitleBuff implements Listener {

    public static final String ID = "HUNGER_DRAIN_REDUCTION";

    public HungerDrainReductionBuff(MysterriaTitles plugin) {
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
    public void onFoodChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        int current = player.getFoodLevel();
        int incoming = event.getFoodLevel();
        if (incoming >= current) return; // not a hunger decrease

        double multiplier = multiplierFor(player.getUniqueId());
        if (multiplier == 1.0) return;

        int deficit = current - incoming;
        int reduced = (int) Math.round(deficit / multiplier);
        event.setFoodLevel(current - reduced);
    }
}
