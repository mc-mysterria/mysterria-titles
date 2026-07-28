package net.mysterria.titles.listener;

import net.mysterria.titles.MysterriaTitles;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;

/**
 * Validating the buff registry inside onEnable() is too early: paper-plugin.yml-based soft
 * dependencies (e.g. Circle of Imagination) always finish enabling after every legacy
 * plugin.yml plugin, regardless of softdepend order, so at that point COI's buffs haven't been
 * registered yet and every COI-tied title would be flagged as inert even though
 * registerCoiIntegration() goes on to wire them up moments later via CoiAvailabilityListener.
 * ServerLoadEvent fires once, after every plugin (of either format) has finished enabling, so
 * validating here reflects the actual final state instead of a mid-boot snapshot.
 */
public class BuffRegistryValidationListener implements Listener {

    private final MysterriaTitles plugin;

    public BuffRegistryValidationListener(MysterriaTitles plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onServerLoad(ServerLoadEvent event) {
        plugin.getBuffManager().validateAgainstRegistry(plugin.getTitleRegistry());
    }
}
