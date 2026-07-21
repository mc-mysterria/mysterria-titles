package net.mysterria.titles.domain.buff.types;

import net.mysterria.titles.MysterriaTitles;
import net.mysterria.titles.domain.buff.model.TitleBuff;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.potion.PotionEffect;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PotionDurationBoostBuff extends TitleBuff implements Listener {

    public static final String ID = "POTION_DURATION_BOOST";

    // EntityPotionEffectEvent's newEffect has no setter - it's informational only, cancellable
    // as a whole. To extend duration we cancel the original application and re-apply a scaled
    // effect ourselves; this guard stops that re-application from re-triggering the same logic.
    private final Set<UUID> reapplying = new HashSet<>();

    public PotionDurationBoostBuff(MysterriaTitles plugin) {
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
    public void onPotionEffect(EntityPotionEffectEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (reapplying.contains(player.getUniqueId())) return;

        PotionEffect newEffect = event.getNewEffect();
        if (newEffect == null) return; // effect removed/cleared, nothing to extend

        double multiplier = multiplierFor(player.getUniqueId());
        if (multiplier == 1.0) return;

        int scaledDuration = (int) Math.round(newEffect.getDuration() * multiplier);
        if (scaledDuration <= newEffect.getDuration()) return;

        event.setCancelled(true);

        PotionEffect boosted = new PotionEffect(
                newEffect.getType(),
                scaledDuration,
                newEffect.getAmplifier(),
                newEffect.isAmbient(),
                newEffect.hasParticles(),
                newEffect.hasIcon()
        );

        reapplying.add(player.getUniqueId());
        try {
            player.addPotionEffect(boosted);
        } finally {
            reapplying.remove(player.getUniqueId());
        }
    }
}
