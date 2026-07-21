package net.mysterria.titles.domain.buff.types;

import net.mysterria.titles.MysterriaTitles;
import net.mysterria.titles.domain.buff.model.TitleBuff;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;

public class SaturationBoostBuff extends TitleBuff implements Listener {

    public static final String ID = "SATURATION_BOOST";

    public SaturationBoostBuff(MysterriaTitles plugin) {
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
    public void onConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        double multiplier = multiplierFor(player.getUniqueId());
        if (multiplier == 1.0) return;

        // Saturation is applied by vanilla after this event resolves, so read the delta a tick later.
        float before = player.getSaturation();
        Bukkit.getScheduler().runTask(plugin, () -> {
            float gained = player.getSaturation() - before;
            if (gained > 0) {
                float bonus = (float) (gained * (multiplier - 1.0));
                player.setSaturation(Math.min(20f, player.getSaturation() + bonus));
            }
        });
    }
}
