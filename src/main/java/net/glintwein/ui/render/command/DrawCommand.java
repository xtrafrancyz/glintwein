package net.glintwein.ui.render.command;

import java.util.List;

public abstract class DrawCommand {
    public abstract Bounds getBounds();

    public boolean isSimilar(DrawCommand other) {
        return this.getClass() == other.getClass();
    }

    public interface Executor<T extends DrawCommand> {
        void execute(List<T> commands);
    }
}
