package net.mysterria.titles.domain.buff.types;

import net.mysterria.titles.MysterriaTitles;
import net.mysterria.titles.domain.buff.model.TitleBuff;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemDamageEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Slows how fast the holder's tools, weapons and armor wear out: a craftsman's touch. Item damage
 * is divided by (1 + value), so 0.3333 means a quarter less wear. Most item damage arrives one
 * point at a time, so the saved fraction is carried per player until it adds up to a whole point;
 * the ordinary Unbreaking roll has already been applied when this event fires.
 */
public class DurabilityWearReductionBuff extends TitleBuff implements Listener {

    public static final String ID = "DURABILITY_WEAR_REDUCTION";

    private final Map<UUID, Double> carry = new HashMap<>();

    public DurabilityWearReductionBuff(MysterriaTitles plugin) {
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
        carry.clear();
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onItemDamage(PlayerItemDamageEvent event) {
        Player player = event.getPlayer();
        double multiplier = multiplierFor(player.getUniqueId());
        if (multiplier == 1.0) {
            carry.remove(player.getUniqueId());
            return;
        }
        double reduced = event.getDamage() / multiplier + carry.getOrDefault(player.getUniqueId(), 0.0);
        int applied = (int) reduced;
        carry.put(player.getUniqueId(), reduced - applied);
        if (applied <= 0) {
            event.setCancelled(true);
            return;
        }
        event.setDamage(applied);
    }
}
