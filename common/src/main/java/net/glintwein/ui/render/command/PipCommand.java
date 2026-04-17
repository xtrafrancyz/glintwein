package net.glintwein.ui.render.command;

import net.glintwein.ui.render.PipAtlasManager;
import org.jetbrains.annotations.NotNull;

public class PipCommand implements Comparable<PipCommand> {
    public final Runnable render;
    public final PipAtlasManager.Sprite sprite;

    public PipCommand(Runnable render, PipAtlasManager.Sprite sprite) {
        this.render = render;
        this.sprite = sprite;
    }

    @Override
    public int compareTo(@NotNull PipCommand o) {
        return Integer.compare(this.sprite.target.getColorTextureId(), o.sprite.target.getColorTextureId());
    }
}
