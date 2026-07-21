package net.mysterria.titles.domain.buff.types;

import net.mysterria.titles.MysterriaTitles;
import net.mysterria.titles.domain.buff.model.TitleBuff;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class DefenseBoostBuff extends TitleBuff implements Listener {

    public static final String ID = "DEFENSE_BOOST";

    public DefenseBoostBuff(MysterriaTitles plugin) {
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
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        // Reduction buffs divide by the same (1 + value) factor boosts multiply by,
        // so a title's bonus.value reads the same way regardless of buff direction.
        double multiplier = multiplierFor(player.getUniqueId());
        if (multiplier != 1.0) {
            event.setDamage(event.getDamage() / multiplier);
        }
    }
}
