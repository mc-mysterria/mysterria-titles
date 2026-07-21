package net.mysterria.titles.listener;

import dev.ua.ikeepcalm.coi.api.event.SequenceChangeEvent;
import net.mysterria.titles.domain.title.service.SequenceTitleAutoGrantService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * Circle of Imagination only - this listener's @EventHandler parameter type comes from that
 * plugin's own event package, so it must never be registered unless
 * CircleOfImaginationHook#isPresent() is true, or Bukkit's reflective handler scan will throw
 * NoClassDefFoundError when COI isn't installed.
 */
public class CoiTitleListener implements Listener {

    private final SequenceTitleAutoGrantService autoGrantService;

    public CoiTitleListener(SequenceTitleAutoGrantService autoGrantService) {
        this.autoGrantService = autoGrantService;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSequenceChange(SequenceChangeEvent event) {
        autoGrantService.evaluate(event.getPlayer());
    }
}
