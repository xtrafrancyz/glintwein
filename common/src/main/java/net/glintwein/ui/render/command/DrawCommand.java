package net.glintwein.ui.render.command;

import net.glintwein.ui.data.Bounds;
import net.glintwein.ui.render.program.GlProgram;
import net.glintwein.ui.render.program.GlintVertexConsumer;

import java.util.List;

public abstract class DrawCommand {
    // Must be set by the constructor of the concrete command class
    public Bounds bounds;
    public Bounds scissor;
    public boolean firstInBatch;

    public boolean isSimilar(DrawCommand other) {
        return this.getClass() == other.getClass() && scissorContainsElement(other.scissor);
    }

    /**
     * Called when the command is no longer needed and can be recycled.
     * Subclasses should override this method to reset any state they hold.
     */
    public void release() {
    }

    public void reset() {
        firstInBatch = false;
        scissor = null;
    }

    private boolean scissorContainsElement(Bounds otherScissor) {
        if (scissor == null)
            return otherScissor == null || otherScissor.contains(bounds);

        return scissor.equals(otherScissor);
    }

    public interface Executor<T extends DrawCommand> {
        void execute(List<T> commands);
    }

    public static abstract class SimpleExecutor<T extends DrawCommand> implements Executor<T> {
        private final GlProgram program;

        public SimpleExecutor(GlProgram program) {
            this.program = program;
        }

        @Override
        public void execute(List<T> commands) {
            if (!program.isValid())
                return;
            program.bind();
            bindUniforms(program, commands.get(0));
            GlintVertexConsumer consumer = program.begin();
            buildVertexBuffer(consumer, commands);
            program.draw();
        }

        protected abstract void bindUniforms(GlProgram program, T first);

        protected abstract void buildVertexBuffer(GlintVertexConsumer consumer, List<T> commands);
    }
}
