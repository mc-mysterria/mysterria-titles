package net.mysterria.titles.domain.title.listener;

import dev.ua.ikeepcalm.coi.api.event.SequenceChangeEvent;
import lombok.RequiredArgsConstructor;
import net.mysterria.titles.domain.title.service.SequenceTitleAutoGrantService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

@RequiredArgsConstructor
public class CoiTitleListener implements Listener {

    private final SequenceTitleAutoGrantService autoGrantService;

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSequenceChange(SequenceChangeEvent event) {
        autoGrantService.evaluate(event.getPlayer());
    }
}
