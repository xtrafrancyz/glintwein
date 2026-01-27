package net.glintwein.ui.render.command;

import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.floats.FloatArrayFIFOQueue;
import net.glintwein.ui.data.BorderRadius;
import net.glintwein.ui.render.font.GigaFont;
import net.glintwein.ui.render.texture.Sprite;
import net.glintwein.ui.util.ARGB;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Context {
    private static final Map<Class<? extends DrawCommand>, DrawCommand.Executor<?>> EXECUTORS = new HashMap<>();

    static {
        EXECUTORS.put(DrawRectCommand.class, new DrawRectCommand.Executor());
        EXECUTORS.put(DrawTextCommand.class, new DrawTextCommand.Executor());
        EXECUTORS.put(DrawTextureCommand.class, new DrawTextureCommand.Executor());
    }

    private final List<DrawCommand> commands = new ArrayList<>();
    private final Matrix3x2fStack transform = new Matrix3x2fStack(16);
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

    public int mulOpacity(int color) {
        float opacity = opacityStack.firstFloat();
        if (opacity == 1)
            return color;
        return ARGB.setAlpha(color, ARGB.alphaF(color) * opacity);
    }

    public void drawRect(float x, float y, float width, float height, int color) {
        drawRect(x, y, width, height, BorderRadius.ZERO, color);
    }

    public void drawRect(float x, float y, float width, float height, BorderRadius radius, int color) {
        if (ARGB.alpha(color = mulOpacity(color)) == 0)
            return;

        addCommand(new DrawRectCommand(
            new Matrix3x2f(transform),
            x, y, x + width, y + height,
            radius,
            color
        ));
    }

    public void drawTexture(Sprite sprite, float x, float y, float width, float height, int color) {
        drawTexture(sprite, x, y, width, height, BorderRadius.ZERO, color);
    }

    public void drawTexture(Sprite sprite, float x, float y, float width, float height, BorderRadius radius, int color) {
        if (ARGB.alpha(color = mulOpacity(color)) == 0)
            return;

        addCommand(new DrawTextureCommand(
            new Matrix3x2f(transform),
            x, y, x + width, y + height,
            sprite.u0, sprite.v0, sprite.u1, sprite.v1,
            radius, sprite.textureId,
            color
        ));
    }

    public void drawText(GigaFont font, String text, float x, float y, float size, int color) {
        if (ARGB.alpha(color = mulOpacity(color)) == 0)
            return;

        addCommand(new DrawTextCommand(
            new Matrix3x2f(transform),
            font, text,
            x, y, size,
            color
        ));
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
        if (commands.isEmpty())
            return;

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
