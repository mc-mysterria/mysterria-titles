package net.mysterria.titles.domain.storage.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import net.mysterria.titles.domain.title.model.PlayerTitleData;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class JsonPlayerDataStore implements PlayerDataStore {

    private record PlayerDataRecord(String uuid, String activeTitle, Set<String> unlocked,
                                     Map<String, Integer> progress, long lastModified) {
    }

    private static final long SHUTDOWN_TIMEOUT_SECONDS = 10;

    private final Path dataFolder;
    private final Logger logger;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    // One thread keeps writes for the same player in submission order, so an older autosave
    // snapshot can never overwrite a newer write (and two writes never share a .tmp file).
    private final ExecutorService executor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "MysterriaTitles-IO");
        thread.setDaemon(true);
        return thread;
    });

    public JsonPlayerDataStore(Path dataFolder, Logger logger) {
        this.dataFolder = dataFolder;
        this.logger = logger;
        try {
            Files.createDirectories(dataFolder);
        } catch (IOException e) {
            logger.severe("Could not create player data folder: " + e.getMessage());
        }
    }

    @Override
    public CompletableFuture<PlayerTitleData> read(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> readSync(uuid), executor);
    }

    private PlayerTitleData readSync(UUID uuid) {
        Path file = pathFor(uuid);
        if (!Files.exists(file)) {
            return new PlayerTitleData(uuid);
        }
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            PlayerDataRecord record = gson.fromJson(reader, PlayerDataRecord.class);
            if (record == null) {
                return new PlayerTitleData(uuid);
            }
            Set<String> unlocked = record.unlocked() != null ? new HashSet<>(record.unlocked()) : new HashSet<>();
            Map<String, Integer> progress = record.progress() != null ? new HashMap<>(record.progress()) : new HashMap<>();
            return new PlayerTitleData(uuid, unlocked, record.activeTitle(), progress);
        } catch (IOException | JsonSyntaxException e) {
            logger.warning("Corrupt player data for " + uuid + ": " + e.getMessage());
            backupCorruptFile(file);
            return new PlayerTitleData(uuid);
        }
    }

    private void backupCorruptFile(Path file) {
        try {
            Path backup = file.resolveSibling(file.getFileName() + ".corrupt-" + System.currentTimeMillis());
            Files.move(file, backup, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ignored) {
        }
    }

    @Override
    public CompletableFuture<Void> write(PlayerTitleData data) {
        return CompletableFuture.runAsync(() -> writeSync(data), executor);
    }

    private void writeSync(PlayerTitleData data) {
        Path file = pathFor(data.getUuid());
        PlayerDataRecord record = new PlayerDataRecord(
                data.getUuid().toString(),
                data.getActiveTitle().orElse(null),
                data.getUnlockedTitles(),
                data.getProgressSnapshot(),
                System.currentTimeMillis()
        );
        Path tmp = file.resolveSibling(file.getFileName() + ".tmp");
        try (Writer writer = Files.newBufferedWriter(tmp, StandardCharsets.UTF_8)) {
            gson.toJson(record, writer);
            Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            logger.warning("Failed to write player data for " + data.getUuid() + ": " + e.getMessage());
        }
    }

    @Override
    public CompletableFuture<Void> writeAll(Collection<PlayerTitleData> data) {
        return CompletableFuture.runAsync(() -> {
            for (PlayerTitleData d : data) {
                writeSync(d);
            }
        }, executor);
    }

    @Override
    public boolean exists(UUID uuid) {
        return Files.exists(pathFor(uuid));
    }

    private Path pathFor(UUID uuid) {
        return dataFolder.resolve(uuid + ".json");
    }

    /**
     * Stops accepting work and waits for queued writes to finish, so data saved on quit or by the
     * final flush reaches disk before the plugin is unloaded.
     */
    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                logger.warning("Player data writes still pending after " + SHUTDOWN_TIMEOUT_SECONDS
                        + "s at shutdown; they may not reach disk.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warning("Interrupted while waiting for player data writes at shutdown.");
        }
    }
}
