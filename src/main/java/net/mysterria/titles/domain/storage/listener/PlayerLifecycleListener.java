package net.mysterria.titles.domain.storage.listener;

import lombok.RequiredArgsConstructor;
import net.mysterria.titles.MysterriaTitles;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

@RequiredArgsConstructor
public class PlayerLifecycleListener implements Listener {

    private final MysterriaTitles plugin;

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        plugin.getPlayerDataManager().load(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        boolean flushOnQuit = plugin.getConfigManager().getSettings().isFlushOnQuit();
        plugin.getPlayerDataManager().unload(event.getPlayer().getUniqueId(), flushOnQuit);
        plugin.getTitleTestModeService().disable(event.getPlayer().getUniqueId());
    }
}
