package net.mysterria.titles.listener;

import net.mysterria.titles.MysterriaTitles;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;

/**
 * PluginEnableEvent is a core Bukkit event, always safe to register regardless of whether
 * Circle of Imagination is installed. This exists because softdepend load order isn't always
 * respected in practice (e.g. jar rebuilt/redeployed without a full server restart) - a plain
 * isPluginEnabled() check inside onEnable() can run before COI has actually finished starting,
 * silently skipping registration forever. Listening here means COI integration activates the
 * moment COI actually comes up, whenever that happens to be.
 */
public final class CoiAvailabilityListener implements Listener {

    private final MysterriaTitles plugin;

    public CoiAvailabilityListener(MysterriaTitles plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPluginEnable(PluginEnableEvent event) {
        if (event.getPlugin().getName().equals("CircleOfImagination")) {
            plugin.registerCoiIntegration();
        }
    }
}
