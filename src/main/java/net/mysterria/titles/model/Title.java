package net.mysterria.titles.model;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;

import java.util.List;

public record Title(
        String id,
        Component display,
        List<Component> description,
        RenderType renderType,
        String nexoTexture,
        String animationFrameBase,
        int animationFrameCount,
        long animationFrameDurationMs,
        TitleBonus bonus,
        Material guiMaterial,
        int guiSlot,
        UnlockMethod unlockMethod,
        String permission
) {

    /**
     * ANIMATED titles have no server-side variant tracking; the frame is derived purely from
     * wall-clock time so every viewer's client is showing the same frame without our plugin
     * needing to broadcast per-player animation state.
     */
    public String resolveTagString() {
        if (renderType == RenderType.ANIMATED) {
            int frame = (int) ((System.currentTimeMillis() / animationFrameDurationMs) % animationFrameCount);
            return "<glyph:" + animationFrameBase + "_" + frame + ">";
        }
        return nexoTexture;
    }
}
