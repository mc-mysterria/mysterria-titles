package net.mysterria.titles.audit;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/** Call only on the main thread. */
public final class AuditContext {

    private AuditContext() {
    }

    public static UUID actorId(CommandSender sender) {
        return sender instanceof Player player ? player.getUniqueId() : null;
    }

    public static String actorRef(CommandSender sender) {
        return sender instanceof Player player ? player.getUniqueId().toString() : "console";
    }

    /** Pre-fills actor_name=console so console rows stay attributable; player names are never recorded. */
    public static Map<String, Object> metadata(CommandSender sender) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (sender != null && !(sender instanceof Player)) {
            result.put("actor_name", "console");
        }
        return result;
    }

    public static void putLocation(Map<String, Object> metadata, Location location) {
        if (location == null || location.getWorld() == null) {
            return;
        }
        metadata.put("world", location.getWorld().getName());
        metadata.put("x", location.getBlockX());
        metadata.put("y", location.getBlockY());
        metadata.put("z", location.getBlockZ());
    }
}
