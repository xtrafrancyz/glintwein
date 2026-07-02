package net.glintwein.ui.render.command;

import net.glintwein.ui.render.PipAtlasManager;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class PipCommand implements Comparable<PipCommand> {
    public final Consumer<PipCommand> render;
    public final PipAtlasManager.Sprite sprite;

    public PipCommand(Consumer<PipCommand> render, PipAtlasManager.Sprite sprite) {
        this.render = render;
        this.sprite = sprite;
    }

    @Override
    public int compareTo(@NotNull PipCommand o) {
        return Integer.compare(this.sprite.textureId(), o.sprite.textureId());
    }
}
