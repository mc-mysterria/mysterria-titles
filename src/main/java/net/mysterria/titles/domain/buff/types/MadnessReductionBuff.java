package net.mysterria.titles.domain.buff.types;

import dev.ua.ikeepcalm.coi.api.event.MadnessGainEvent;
import net.mysterria.titles.MysterriaTitles;
import net.mysterria.titles.domain.buff.model.TitleBuff;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

/**
 * Circle of Imagination only - see MagicDamageBoostBuff for why registration must be guarded
 * behind CircleOfImaginationHook#isPresent().
 */
public class MadnessReductionBuff extends TitleBuff implements Listener {

    public static final String ID = "MADNESS_REDUCTION";

    public MadnessReductionBuff(MysterriaTitles plugin) {
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
    public void onMadnessGain(MadnessGainEvent event) {
        double multiplier = multiplierFor(event.getPlayer().getUniqueId());
        if (multiplier != 1.0) {
            event.setAmount(event.getAmount() / multiplier);
        }
    }
}
