package net.mysterria.titles.domain.title.listener;

import lombok.RequiredArgsConstructor;
import net.mysterria.titles.MysterriaTitles;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;

@RequiredArgsConstructor
public class CoiAvailabilityListener implements Listener {

    private final MysterriaTitles plugin;

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPluginEnable(PluginEnableEvent event) {
        if (event.getPlugin().getName().equals("CircleOfImagination")) {
            plugin.registerCoiIntegration();
        }
    }
}
