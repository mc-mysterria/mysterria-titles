package net.mysterria.titles.domain.title.model;

import lombok.Getter;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class PlayerTitleData {

    @Getter
    private final UUID uuid;
    private final Set<String> unlockedTitles;
    private String activeTitle;
    private transient boolean dirty;

    public PlayerTitleData(UUID uuid) {
        this(uuid, new HashSet<>(), null);
    }

    public PlayerTitleData(UUID uuid, Set<String> unlockedTitles, String activeTitle) {
        this.uuid = uuid;
        this.unlockedTitles = new HashSet<>(unlockedTitles);
        this.activeTitle = activeTitle;
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
        return new PlayerTitleData(uuid, unlockedTitles, activeTitle);
    }
}
