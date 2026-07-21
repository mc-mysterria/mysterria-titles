package net.mysterria.titles.domain.storage.model;

import net.mysterria.titles.domain.title.model.PlayerTitleData;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface PlayerDataStore {

    CompletableFuture<PlayerTitleData> read(UUID uuid);

    CompletableFuture<Void> write(PlayerTitleData data);

    CompletableFuture<Void> writeAll(Collection<PlayerTitleData> data);

    boolean exists(UUID uuid);
}
