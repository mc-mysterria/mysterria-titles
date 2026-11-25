package net.mysterria.titles.api.event;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

/**
 * Fired after a title has actually been stored on a player, on the main thread.
 * A failed or duplicate grant does not fire this event.
 */
public class TitleGrantedEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final UUID player;
    private final String titleId;
    private final String source;

    public TitleGrantedEvent(UUID player, String titleId, String source) {
        this.player = player;
        this.titleId = titleId;
        this.source = source;
    }

    /** The id of the player who received the title. */
    public UUID getPlayer() {
        return player;
    }

    /** Alias of {@link #getPlayer()} with an explicit name. */
    public UUID getPlayerUuid() {
        return player;
    }

    /** The configured id of the title that was granted. */
    public String getTitleId() {
        return titleId;
    }

    /** Short label for the route that granted the title, for example "admin-grant". */
    public String getSource() {
        return source;
    }

    /** Calls this event through Bukkit. Listener failures are handled by Bukkit as usual. */
    public static void fire(UUID player, String titleId, String source) {
        Bukkit.getPluginManager().callEvent(new TitleGrantedEvent(player, titleId, source));
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
}
