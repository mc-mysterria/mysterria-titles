package net.mysterria.titles.domain.storage.service;

import lombok.RequiredArgsConstructor;
import net.mysterria.titles.domain.title.model.PlayerTitleData;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class PlayerDataManager {

    private final Plugin plugin;
    private final JsonPlayerDataStore store;
    private final Map<UUID, PlayerTitleData> cache = new ConcurrentHashMap<>();
    private int autosaveTaskId = -1;

    public CompletableFuture<PlayerTitleData> load(UUID uuid) {
        PlayerTitleData cached = cache.get(uuid);
        if (cached != null) {
            return CompletableFuture.completedFuture(cached);
        }
        return store.read(uuid).thenApply(data -> {
            PlayerTitleData existing = cache.putIfAbsent(uuid, data);
            return existing != null ? existing : data;
        });
    }

    public PlayerTitleData getCached(UUID uuid) {
        return cache.get(uuid);
    }

    public void unload(UUID uuid, boolean flush) {
        PlayerTitleData data = cache.remove(uuid);
        if (flush && data != null && data.isDirty()) {
            store.write(data.snapshot());
            data.clearDirty();
        }
    }

    public CompletableFuture<Void> flushAll() {
        List<PlayerTitleData> dirty = cache.values().stream()
                .filter(PlayerTitleData::isDirty)
                .map(PlayerTitleData::snapshot)
                .toList();
        cache.values().forEach(data -> {
            if (data.isDirty()) data.clearDirty();
        });
        if (dirty.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }
        return store.writeAll(dirty);
    }

    public void startAutosaveTask(long intervalTicks) {
        autosaveTaskId = Bukkit.getScheduler()
                .runTaskTimerAsynchronously(plugin, this::flushAll, intervalTicks, intervalTicks)
                .getTaskId();
    }

    public void stopAutosaveTask() {
        if (autosaveTaskId != -1) {
            Bukkit.getScheduler().cancelTask(autosaveTaskId);
            autosaveTaskId = -1;
        }
    }
}
