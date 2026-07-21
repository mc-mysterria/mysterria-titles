package net.mysterria.titles.domain.buff.types;

import com.destroystokyo.paper.event.entity.EntityKnockbackByEntityEvent;
import net.mysterria.titles.MysterriaTitles;
import net.mysterria.titles.domain.buff.model.TitleBuff;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

public class KnockbackResistanceBoostBuff extends TitleBuff implements Listener {

    public static final String ID = "KNOCKBACK_RESISTANCE_BOOST";

    public KnockbackResistanceBoostBuff(MysterriaTitles plugin) {
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
    public void onKnockback(EntityKnockbackByEntityEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        double multiplier = multiplierFor(player.getUniqueId());
        if (multiplier != 1.0) {
            event.setKnockback(event.getKnockback().multiply(1.0 / multiplier));
        }
    }
}
