package net.mysterria.titles.domain.title.model.source;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TitleFilter {
    ALL("All titles"),
    OWNED("Owned titles"),
    LOCKED("Locked titles");

    private final String label;

    public boolean accepts(boolean unlocked) {
        return this == ALL || unlocked == (this == OWNED);
    }

    public TitleFilter next() {
        return values()[(ordinal() + 1) % values().length];
    }
}
