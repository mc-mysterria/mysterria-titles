package net.mysterria.titles.buff.impl;

import net.mysterria.titles.MysterriaTitles;
import net.mysterria.titles.buff.TitleBuff;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerExpChangeEvent;

public final class ExpBoostBuff extends TitleBuff implements Listener {

    public static final String ID = "EXP_BOOST";

    public ExpBoostBuff(MysterriaTitles plugin) {
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
    public void onExpChange(PlayerExpChangeEvent event) {
        double multiplier = multiplierFor(event.getPlayer().getUniqueId());
        if (multiplier != 1.0) {
            event.setAmount((int) Math.round(event.getAmount() * multiplier));
        }
    }
}
