package net.mysterria.titles.domain.title.model.source;

public enum UnlockMethod {
    PERMISSION,
    COMMAND,
    /**
     * Granted by internal plugin logic (e.g. SequenceTitleAutoGrantService), not staff. Gating
     * behaviour is identical to COMMAND - both just check the player's stored unlockedTitles
     * set - this only exists to show an accurate hint in the locked-item lore instead of
     * "Granted by staff".
     */
    AUTO
}
