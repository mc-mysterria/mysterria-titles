package net.mysterria.titles.domain.buff.types;

import net.mysterria.titles.MysterriaTitles;
import net.mysterria.titles.domain.buff.model.TitleBuff;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityAirChangeEvent;

/**
 * Slows how fast a player loses air underwater. A value of 0.5 means each breath of air is
 * drained at 1/1.5 of the normal rate, so the player can stay under roughly 50% longer.
 * Air regain above water is untouched.
 */
public class WaterBreathingBoostBuff extends TitleBuff implements Listener {

    public static final String ID = "WATER_BREATHING_BOOST";

    /** Fractional air saved per player so slow drains still add up correctly over time. */
    private final java.util.Map<java.util.UUID, Double> carry = new java.util.HashMap<>();

    public WaterBreathingBoostBuff(MysterriaTitles plugin) {
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
    public void onAirChange(EntityAirChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        int current = player.getRemainingAir();
        int incoming = event.getAmount();
        if (incoming >= current) return; // regaining air, not drowning
        double multiplier = multiplierFor(player.getUniqueId());
        if (multiplier == 1.0) {
            carry.remove(player.getUniqueId());
            return;
        }
        double reduced = (current - incoming) / multiplier + carry.getOrDefault(player.getUniqueId(), 0.0);
        int applied = (int) reduced;
        carry.put(player.getUniqueId(), reduced - applied);
        event.setAmount(current - applied);
    }
}
