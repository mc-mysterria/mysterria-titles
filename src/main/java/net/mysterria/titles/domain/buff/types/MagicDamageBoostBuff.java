package net.mysterria.titles.domain.buff.types;

import dev.ua.ikeepcalm.coi.api.event.MagicDamageEvent;
import net.mysterria.titles.MysterriaTitles;
import net.mysterria.titles.domain.buff.model.TitleBuff;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

/**
 * Circle of Imagination only - this listener's @EventHandler parameter type comes from that
 * plugin's own event package, so it must never be registered (see TitleBuff#register) unless
 * CircleOfImaginationHook#isPresent() is true, or Bukkit's reflective handler scan will throw
 * NoClassDefFoundError when COI isn't installed.
 */
public class MagicDamageBoostBuff extends TitleBuff implements Listener {

    public static final String ID = "MAGIC_DAMAGE_BOOST";

    public MagicDamageBoostBuff(MysterriaTitles plugin) {
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
    public void onMagicDamage(MagicDamageEvent event) {
        Player damager = event.getDamager();
        if (damager == null) return;

        double multiplier = multiplierFor(damager.getUniqueId());
        if (multiplier != 1.0) {
            event.setDamage(event.getDamage() * multiplier);
        }
    }
}
