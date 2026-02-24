package net.glintwein.ui.render.command;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.floats.FloatArrayFIFOQueue;
import net.glintwein.ui.data.BorderRadius;
import net.glintwein.ui.data.Bounds;
import net.glintwein.ui.data.Box;
import net.glintwein.ui.data.Gradient;
import net.glintwein.ui.render.font.GigaFont;
import net.glintwein.ui.render.texture.Sprite;
import net.glintwein.ui.util.ARGB;
import net.glintwein.ui.util.GMath;
import net.minecraft.client.Minecraft;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;

import java.util.*;

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
    private final Deque<Bounds> scissorStack = new ArrayDeque<>();

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

    public boolean pushScissor(Bounds bounds) {
        bounds.transformAxisAligned(transform);
        Bounds last = scissorStack.peekLast();
        if (last != null) {
            bounds = last.intersection(bounds);
            if (bounds == null)
                return false;
        }
        scissorStack.addLast(bounds);
        return true;
    }

    public void popScissor() {
        scissorStack.removeLast();
    }

    public int mulOpacity(int color) {
        float opacity = opacityStack.firstFloat();
        if (opacity == 1)
            return color;
        return ARGB.mulAlpha(color, opacity);
    }

    public Gradient mulOpacity(Gradient gradient) {
        float opacity = opacityStack.firstFloat();
        if (opacity == 1)
            return gradient;
        return gradient.mulAlpha(opacity);
    }

    public void drawRect(Box box, int color) {
        drawRect(box.x, box.y, box.width, box.height, BorderRadius.ZERO, color);
    }

    public void drawRect(float x, float y, float width, float height, int color) {
        drawRect(x, y, width, height, BorderRadius.ZERO, color);
    }

    public void drawRect(Box box, BorderRadius radius, int color) {
        drawRect(box.x, box.y, box.width, box.height, radius, color);
    }

    public void drawRect(float x, float y, float width, float height, BorderRadius radius, int color) {
        if (ARGB.alpha(color = mulOpacity(color)) == 0)
            return;

        addCommand(new DrawRectCommand(
            new Matrix3x2f(transform),
            x, y, x + width, y + height,
            radius,
            color,
            0, 0
        ));
    }

    public void drawRect(Box box, BorderRadius radius, Gradient color) {
        drawRect(box.x, box.y, box.width, box.height, radius, color);
    }

    public void drawRect(float x, float y, float width, float height, BorderRadius radius, Gradient color) {
        color = mulOpacity(color);
        if (color.isFullyTransparent())
            return;
        addCommand(new DrawRectCommand(
            new Matrix3x2f(transform),
            x, y, x + width, y + height,
            radius,
            color.topLeft(), color.topRight(), color.bottomRight(), color.bottomLeft(),
            0, 0
        ));
    }

    public void drawRect(DrawRectBuilder builder) {
        int outlineColor = mulOpacity(builder.outlineColor);
        boolean hasOutline = ARGB.alpha(outlineColor) != 0 && builder.outlineWidth > 0;
        int color00, color10, color11, color01;
        if (builder.gradient != null) {
            builder.gradient = mulOpacity(builder.gradient);
            if (builder.gradient.isFullyTransparent() && !hasOutline)
                return;
            color00 = builder.gradient.topLeft();
            color10 = builder.gradient.topRight();
            color11 = builder.gradient.bottomRight();
            color01 = builder.gradient.bottomLeft();
        } else {
            builder.color = mulOpacity(builder.color);
            if (ARGB.alpha(builder.color) == 0 && !hasOutline)
                return;
            color00 = color10 = color11 = color01 = builder.color;
        }

        addCommand(new DrawRectCommand(
            new Matrix3x2f(transform),
            builder.x0, builder.y0, builder.x1, builder.y1,
            builder.radius,
            color00, color10, color11, color01,
            outlineColor, builder.outlineWidth
        ));
    }

    public void drawTexture(Sprite sprite, Box box, int color) {
        drawTexture(sprite, box.x, box.y, box.width, box.height, BorderRadius.ZERO, color);
    }

    public void drawTexture(Sprite sprite, float x, float y, float width, float height, int color) {
        drawTexture(sprite, x, y, width, height, BorderRadius.ZERO, color);
    }

    public void drawTexture(Sprite sprite, Box box, BorderRadius radius, int color) {
        drawTexture(sprite, box.x, box.y, box.width, box.height, radius, color);
    }

    public void drawTexture(Sprite sprite, float x, float y, float width, float height, BorderRadius radius, int color) {
        if (ARGB.alpha(color = mulOpacity(color)) == 0)
            return;

        addCommand(new DrawTextureCommand(
            new Matrix3x2f(transform),
            x, y, x + width, y + height,
            sprite.u0, sprite.v0, sprite.u1, sprite.v1,
            radius, sprite.textureId,
            color,
            0, 0
        ));
    }

    public void drawTexture(DrawTextureBuilder builder) {
        int outlineColor = mulOpacity(builder.outlineColor);
        boolean hasOutline = ARGB.alpha(outlineColor) != 0 && builder.outlineWidth > 0;
        int color = mulOpacity(builder.color);
        if (ARGB.alpha(color) == 0 && !hasOutline)
            return;

        addCommand(new DrawTextureCommand(
            new Matrix3x2f(transform),
            builder.x0, builder.y0, builder.x1, builder.y1,
            builder.u0, builder.v0, builder.u1, builder.v1,
            builder.radius,
            builder.texture, color,
            outlineColor, builder.outlineWidth
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
        cmd.scissor = scissorStack.peekLast();
        if (cmd.scissor != null && !cmd.scissor.intersects(cmd.getBounds()))
            return;

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
        new ListExecutor(commands).execute();
        commands.clear();
    }

    private static class ListExecutor {
        private final List<DrawCommand> commands;

        public ListExecutor(List<DrawCommand> commands) {
            this.commands = commands;
        }

        public void execute() {
            if (commands.isEmpty())
                return;

            RenderSystem.disableCull();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableAlphaTest();
            RenderSystem.disableDepthTest();

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

            RenderSystem.disableScissor();
            RenderSystem.enableAlphaTest();
            RenderSystem.enableDepthTest();
        }

        @SuppressWarnings("unchecked")
        private <T extends DrawCommand> void executeBatch(Class<? extends DrawCommand> cmdClass, List<DrawCommand> batch) {
            DrawCommand.Executor<T> executor = (DrawCommand.Executor<T>) EXECUTORS.get(cmdClass);
            if (executor != null) {
                DrawCommand cmd = batch.get(0);
                if (cmd.scissor != null) {
                    enableScissor(cmd.scissor);
                } else {
                    RenderSystem.disableScissor();
                }
                executor.execute((List<T>) batch);
            }
        }

        private static void enableScissor(Bounds bounds) {
            Window window = Minecraft.getInstance().getWindow();
            float scale = 1;//(float) window.getGuiScale();
            int width = GMath.ceil((bounds.maxX - bounds.minX) * scale);
            int height = GMath.ceil((bounds.maxY - bounds.minY) * scale);
            int x = GMath.floor(bounds.minX * scale);
            int y = GMath.floor(window.getHeight() - bounds.maxY * scale);
            RenderSystem.enableScissor(x, y, width, height);
        }
    }
}
