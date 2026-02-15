package net.glintwein.ui.render.command;

import net.glintwein.ui.data.Bounds;

import java.util.List;
import java.util.Objects;

public abstract class DrawCommand {
    public Bounds scissor;

    public abstract Bounds getBounds();

    public boolean isSimilar(DrawCommand other) {
        return this.getClass() == other.getClass() && Objects.equals(scissor, other.scissor);
    }

    public interface Executor<T extends DrawCommand> {
        void execute(List<T> commands);
    }
}
