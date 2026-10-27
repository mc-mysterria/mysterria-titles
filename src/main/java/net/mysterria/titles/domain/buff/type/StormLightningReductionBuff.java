package net.mysterria.titles.domain.buff.type;

import net.mysterria.titles.MysterriaTitles;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.persistence.PersistentDataType;

public class StormLightningReductionBuff extends TitleBuff implements Listener {

    public static final String ID = "STORM_LIGHTNING_REDUCTION";

    private final NamespacedKey weatherKey;

    public StormLightningReductionBuff(MysterriaTitles plugin) {
        super(plugin);
        this.weatherKey = new NamespacedKey(plugin, "weather_lightning");
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

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onStrike(LightningStrikeEvent event) {
        if (event.getCause() != LightningStrikeEvent.Cause.WEATHER) return;
        event.getLightning().getPersistentDataContainer().set(weatherKey, PersistentDataType.BOOLEAN, true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof LightningStrike lightning)) return;
        if (!lightning.getPersistentDataContainer().has(weatherKey)) return;
        if (!(event.getEntity() instanceof Player player)) return;

        double multiplier = multiplierFor(player.getUniqueId());
        if (multiplier != 1.0) {
            event.setDamage(event.getDamage() / multiplier);
        }
    }
}
