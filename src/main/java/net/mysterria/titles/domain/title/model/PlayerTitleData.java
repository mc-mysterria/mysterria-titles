package net.mysterria.titles.domain.title.model;

import lombok.Getter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class PlayerTitleData {

    @Getter
    private final UUID uuid;
    private final Set<String> unlockedTitles;
    private final Map<String, Integer> progress;
    private String activeTitle;
    private transient boolean dirty;

    public PlayerTitleData(UUID uuid) {
        this(uuid, new HashSet<>(), null, new HashMap<>());
    }

    public PlayerTitleData(UUID uuid, Set<String> unlockedTitles, String activeTitle) {
        this(uuid, unlockedTitles, activeTitle, new HashMap<>());
    }

    public PlayerTitleData(UUID uuid, Set<String> unlockedTitles, String activeTitle, Map<String, Integer> progress) {
        this.uuid = uuid;
        this.unlockedTitles = new HashSet<>(unlockedTitles);
        this.activeTitle = activeTitle;
        this.progress = new HashMap<>(progress);
    }

    public boolean unlock(String titleId) {
        boolean added = unlockedTitles.add(titleId);
        if (added) dirty = true;
        return added;
    }

    public boolean revoke(String titleId) {
        boolean removed = unlockedTitles.remove(titleId);
        if (removed) {
            dirty = true;
            if (titleId.equals(activeTitle)) {
                activeTitle = null;
            }
        }
        return removed;
    }

    public boolean hasUnlocked(String titleId) {
        return unlockedTitles.contains(titleId);
    }

    public Optional<String> getActiveTitle() {
        return Optional.ofNullable(activeTitle);
    }

    public boolean setActiveTitle(String titleId) {
        if (!unlockedTitles.contains(titleId)) return false;
        this.activeTitle = titleId;
        this.dirty = true;
        return true;
    }

    public boolean clearActiveTitle() {
        if (activeTitle == null) return false;
        activeTitle = null;
        dirty = true;
        return true;
    }

    public Set<String> getUnlockedTitles() {
        return Set.copyOf(unlockedTitles);
    }

    public int getProgress(String titleId) {
        return progress.getOrDefault(titleId, 0);
    }

    /**
     * Adds to a title's progress counter (e.g. Advent Calendar days claimed) and returns the new
     * total. Purely a counter - callers decide what to do once it reaches a required threshold.
     */
    public int addProgress(String titleId, int amount) {
        int total = progress.merge(titleId, amount, Integer::sum);
        dirty = true;
        return total;
    }

    /**
     * Overwrites a title's progress counter outright - for admin corrections (e.g. undoing
     * points added by a misconfigured reward source) rather than normal incremental progress.
     */
    public void setProgress(String titleId, int amount) {
        progress.put(titleId, amount);
        dirty = true;
    }

    public Map<String, Integer> getProgressSnapshot() {
        return Map.copyOf(progress);
    }

    public boolean isDirty() {
        return dirty;
    }

    public void markDirty() {
        this.dirty = true;
    }

    public void clearDirty() {
        this.dirty = false;
    }

    public PlayerTitleData snapshot() {
        return new PlayerTitleData(uuid, unlockedTitles, activeTitle, progress);
    }
}
