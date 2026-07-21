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
 * Circle of Imagination only - see MagicDamageBoostBuff for why registration must be guarded
 * behind CircleOfImaginationHook#isPresent().
 */
public class MagicDefenseBoostBuff extends TitleBuff implements Listener {

    public static final String ID = "MAGIC_DEFENSE_BOOST";

    public MagicDefenseBoostBuff(MysterriaTitles plugin) {
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
        if (!(event.getDamaged() instanceof Player player)) return;

        double multiplier = multiplierFor(player.getUniqueId());
        if (multiplier != 1.0) {
            event.setDamage(event.getDamage() / multiplier);
        }
    }
}
