package net.mysterria.titles.domain.buff.listener;

import lombok.RequiredArgsConstructor;
import net.mysterria.titles.MysterriaTitles;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;

@RequiredArgsConstructor
public class BuffRegistryValidationListener implements Listener {

    private final MysterriaTitles plugin;

    @EventHandler
    public void onServerLoad(ServerLoadEvent event) {
        plugin.getBuffManager().validateAgainstRegistry(plugin.getTitleRegistry());
    }
}
