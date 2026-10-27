package net.mysterria.titles.domain.title.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.mysterria.titles.domain.title.model.source.RenderType;
import net.mysterria.titles.domain.title.model.source.UnlockMethod;
import org.bukkit.Material;

import java.util.List;

@Getter
@AllArgsConstructor
public class Title {

    private final String id;
    private final Component display;
    private final List<Component> description;
    private final RenderType renderType;
    private final String nexoTexture;
    private final String animationFrameBase;
    private final int animationFrameCount;
    private final long animationFrameDurationMs;
    private final TitleBonus bonus;
    private final Material guiMaterial;
    private final UnlockMethod unlockMethod;
    private final String permission;
    private final int progressRequired;

    public String resolveTagString() {
        if (renderType == RenderType.ANIMATED) {
            int frame = (int) ((System.currentTimeMillis() / animationFrameDurationMs) % animationFrameCount);
            return "<glyph:" + animationFrameBase + "_" + frame + ">";
        }
        return nexoTexture;
    }
}
