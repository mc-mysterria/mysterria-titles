package net.mysterria.titles.domain.buff.type;

import net.mysterria.titles.MysterriaTitles;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityAirChangeEvent;

public class WaterBreathingBoostBuff extends TitleBuff implements Listener {

    public static final String ID = "WATER_BREATHING_BOOST";

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
        if (incoming >= current) return;
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
