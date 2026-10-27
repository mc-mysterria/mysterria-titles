package net.mysterria.titles.domain.title.model.source;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UnlockMethod {
    PERMISSION("Comes with a rank or perk"),
    COMMAND("Awarded by staff or at events"),
    AUTO("Unlocks on its own through play");

    private final String hint;
}
