package net.mysterria.titles.api;

import java.util.UUID;

/**
 * Small public hook for other plugins that need to read or grant MysterriaTitles titles.
 * Registered with Bukkit's ServicesManager while MysterriaTitles is enabled.
 * Call all methods on the main thread.
 */
public interface TitlesService {

    /**
     * Grants a title through the normal unlock path, so the stored data is marked dirty and
     * saved by the plugin's autosave task, and name tags refresh as usual. No chat message is
     * sent by this call. A {@link net.mysterria.titles.api.event.TitleGrantedEvent} is fired
     * only when the title is newly held.
     *
     * <p>Only players with loaded title data can be changed. Online players are loaded on
     * join and on startup, so this works for them. Offline players are left untouched and
     * return {@code false}. Unknown title ids also return {@code false}.
     *
     * @param player  the player's unique id
     * @param titleId a defined title id, see {@link #titleIds()}
     * @param source  short label describing the caller, used as the event source; blank becomes "api"
     * @return true if the player did not already hold the title and it was granted
     */
    boolean grant(UUID player, String titleId, String source);

    /**
     * Reads a title directly from the player's stored unlocks. Permission-based titles the
     * player can see in the menu are not counted here.
     *
     * <p>Offline players have no loaded data and return {@code false}.
     *
     * @param player  the player's unique id
     * @param titleId a title id
     * @return true only if the player is loaded and holds the title
     */
    boolean has(UUID player, String titleId);

}
