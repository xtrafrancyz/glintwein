package net.glintwein.ui.render.command;

import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.floats.FloatArrayFIFOQueue;
import net.glintwein.ui.render.font.GigaFont;
import org.joml.Matrix3x2fStack;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Context {
    private static final Map<Class<? extends DrawCommand>, DrawCommand.Executor<?>> EXECUTORS = new HashMap<>();

    static {
        EXECUTORS.put(DrawRectCommand.class, new DrawRectCommand.Executor());
        EXECUTORS.put(DrawTextCommand.class, new DrawTextCommand.Executor());
    }

    private final List<DrawCommand> commands = new ArrayList<>();
    private final Matrix3x2fStack transform = new Matrix3x2fStack(4);
    private final FloatArrayFIFOQueue opacityStack = new FloatArrayFIFOQueue();

    public Context() {
        this.opacityStack.enqueue(1.0f);
    }

    public Matrix3x2fStack pose() {
        return transform;
    }

    public float pushOpacity(float opacity) {
        float newOpacity = opacityStack.firstFloat() * opacity;
        opacityStack.enqueueFirst(newOpacity);
        return newOpacity;
    }

    public void popOpacity() {
        opacityStack.dequeueFloat();
    }

    public void drawRect(float x, float y, float width, float height, int color) {
        Vector2f temp = new Vector2f();
        transform.transformPosition(x, y, temp);
        x = temp.x;
        y = temp.y;
        transform.transformPosition(width, height, temp);
        width = temp.x;
        height = temp.y;
        addCommand(new DrawRectCommand(x, y, width, height, color));
    }

    public void drawText(GigaFont font, String text, float x, float y, float size, int color) {
        addCommand(new DrawTextCommand(font, text, x, y, size, color));
    }

    private void addCommand(DrawCommand cmd) {
        // Inspiration
        // https://github.com/nxp-imx/gtec-demo-framework/blob/master/Doc/FslSimpleUI.md#imbatch-flexrendersystem
        // https://github.com/nxp-imx/gtec-demo-framework/blob/e8840f58de1ebd0233391a663d15c4ca8a45d35c/DemoFramework/FslSimpleUI/Render/IMBatch/source/FslSimpleUI/Render/IMBatch/Preprocess/Linear/LinearPreprocessor.hpp#L142
        int maxBacktrack = 16;
        int targetIndex = commands.size() - 1;
        while (targetIndex >= 0 && maxBacktrack-- > 0) {
            DrawCommand prev = commands.get(targetIndex);
            if (!prev.isSimilar(cmd) && !prev.getBounds().intersects(cmd.getBounds())) {
                targetIndex -= 1;
                continue;
            }

            commands.add(targetIndex + 1, cmd);
            return;
        }

        // no previous task found, just add it to the end
        commands.add(cmd);
    }

    public void execute() {
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        int startIndex = 0;
        DrawCommand currentCommand = commands.get(0);
        for (int i = 1; i < commands.size(); i++) {
            DrawCommand cmd = commands.get(i);
            if (!currentCommand.isSimilar(cmd)) {
                executeBatch(currentCommand.getClass(), commands.subList(startIndex, i));
                startIndex = i;
                currentCommand = cmd;
            }
        }
        executeBatch(currentCommand.getClass(), commands.subList(startIndex, commands.size()));
        commands.clear();
    }

    @SuppressWarnings("unchecked")
    private <T extends DrawCommand> void executeBatch(Class<? extends DrawCommand> cmdClass, List<DrawCommand> batch) {
        DrawCommand.Executor<T> executor = (DrawCommand.Executor<T>) EXECUTORS.get(cmdClass);
        if (executor != null) {
            executor.execute((List<T>) batch);
        }
    }
}
