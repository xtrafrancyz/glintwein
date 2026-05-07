package net.glintwein.ui.render.command;

import net.glintwein.ui.data.Bounds;

import java.util.List;

public abstract class DrawCommand {
    // Must be set by the constructor of the concrete command class
    public Bounds bounds;
    public Bounds scissor;
    public boolean firstInBatch;

    public boolean isSimilar(DrawCommand other) {
        return this.getClass() == other.getClass() && scissorContainsElement(other.scissor);
    }

    private boolean scissorContainsElement(Bounds otherScissor) {
        if (scissor == null)
            return otherScissor == null || otherScissor.contains(bounds);

        return scissor.equals(otherScissor);
    }

    public interface Executor<T extends DrawCommand> {
        void execute(List<T> commands);
    }
}
