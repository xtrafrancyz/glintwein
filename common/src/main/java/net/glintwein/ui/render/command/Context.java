package net.glintwein.ui.render.command;

import it.unimi.dsi.fastutil.floats.FloatArrayFIFOQueue;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.glintwein.platform.Platform;
import net.glintwein.ui.data.BorderRadius;
import net.glintwein.ui.data.Bounds;
import net.glintwein.ui.data.Box;
import net.glintwein.ui.data.Gradient;
import net.glintwein.ui.render.PipAtlasManager;
import net.glintwein.ui.render.font.GigaFont;
import net.glintwein.ui.render.program.GlProgramInvalidException;
import net.glintwein.ui.render.texture.AtlasPacker;
import net.glintwein.ui.render.texture.Sprite;
import net.glintwein.ui.util.ARGB;
import net.glintwein.ui.util.GMath;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;
import org.joml.Vector2f;
import org.lwjgl.opengl.GL20;

import java.util.*;
import java.util.function.Consumer;

public class Context {
    private static final Bounds UNBOUNDED = Bounds.fromMinMax(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
    private static final Map<Class<? extends DrawCommand>, DrawCommand.Executor<?>> EXECUTORS = new HashMap<>();

    static {
        registerExecutor(DrawRectCommand.class, new DrawRectCommand.Executor());
        registerExecutor(DrawTextCommand.class, new DrawTextCommand.Executor());
        registerExecutor(DrawTextureCommand.class, new DrawTextureCommand.Executor());
        registerExecutor(DrawShadowCommand.class, new DrawShadowCommand.Executor());
    }

    public static <T extends DrawCommand> void registerExecutor(Class<T> cmdClass, DrawCommand.Executor<T> executor) {
        EXECUTORS.put(cmdClass, executor);
    }

    private final PriorityQueue<PipCommand> pipCommands = new PriorityQueue<>();
    private final Matrix3x2fStack transform = new Matrix3x2fStack(32);
    private final FloatArrayFIFOQueue opacityStack = new FloatArrayFIFOQueue();
    private final Deque<Bounds> scissorStack = new ArrayDeque<>();

    private final Int2ObjectMap<List<DrawCommand>> priorityCommandMap = new Int2ObjectOpenHashMap<>(4);
    private int currentPriority = 0;

    public Context() {
        reset();
    }

    public void reset() {
        transform.clear();
        opacityStack.clear();
        opacityStack.enqueue(1.0f);
        scissorStack.clear();
        for (List<DrawCommand> list : priorityCommandMap.values())
            for (DrawCommand cmd : list)
                cmd.release();
        priorityCommandMap.clear();
        currentPriority = 0;
        for (PipCommand cmd : pipCommands)
            cmd.sprite.release();
        pipCommands.clear();
    }

    public Matrix3x2fStack pose() {
        return transform;
    }

    public float getPixelSize() {
        return 1 / ((transform.m00 + transform.m11) * 0.5f);
    }

    public float pushOpacityExact(float opacity) {
        opacityStack.enqueueFirst(opacity);
        return opacity;
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
            bounds = last.intersectionOrNull(bounds);
            if (bounds == null)
                return false;
        }
        scissorStack.addLast(bounds);
        return true;
    }

    public void popScissor() {
        scissorStack.removeLast();
    }

    private int mulOpacity(int color) {
        float opacity = opacityStack.firstFloat();
        if (opacity == 1)
            return color;
        return ARGB.mulAlpha(color, opacity);
    }

    private Gradient mulOpacity(Gradient gradient) {
        float opacity = opacityStack.firstFloat();
        if (opacity == 1)
            return gradient;
        return gradient.mulAlpha(opacity);
    }

    public void pushDrawPriority(int addedPriority) {
        currentPriority += addedPriority;
        scissorStack.addLast(UNBOUNDED);
    }

    public void popDrawPriority(int subtractedPriority) {
        currentPriority -= subtractedPriority;
        scissorStack.removeLast();
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

        addCommand(DrawRectCommand.POOL.acquire().set(
            transform,
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
        addCommand(DrawRectCommand.POOL.acquire().set(
            transform,
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

        addCommand(DrawRectCommand.POOL.acquire().set(
            transform,
            builder.x0, builder.y0, builder.x1, builder.y1,
            builder.radius,
            color00, color10, color11, color01,
            outlineColor, builder.outlineWidth
        ));
    }

    public void drawShadow(Box box, BorderRadius radius, int color, float blurSpread) {
        color = mulOpacity(color);
        if (ARGB.alpha(color) == 0)
            return;
        addCommand(DrawShadowCommand.POOL.acquire().set(
            transform,
            box.x, box.y, box.x + box.width, box.y + box.height,
            radius,
            color, color, color, color,
            blurSpread
        ));
    }

    public void drawShadow(DrawShadowBuilder builder) {
        int color00, color10, color11, color01;
        color00 = mulOpacity(builder.colorTL);
        color10 = mulOpacity(builder.colorTR);
        color11 = mulOpacity(builder.colorBR);
        color01 = mulOpacity(builder.colorBL);
        if (ARGB.alpha(color00) == 0 && ARGB.alpha(color10) == 0 && ARGB.alpha(color11) == 0 && ARGB.alpha(color01) == 0)
            return;

        addCommand(DrawShadowCommand.POOL.acquire().set(
            transform,
            builder.x0, builder.y0, builder.x1, builder.y1,
            builder.radius,
            color00, color10, color11, color01,
            builder.blurSpread
        ));
    }

    public void drawLine(float x0, float y0, float x1, float y1, float width, BorderRadius radius, int color) {
        if (ARGB.alpha(color = mulOpacity(color)) == 0)
            return;
        drawLineInner(x0, y0, x1, y1, width, radius, color, color, color, color);
    }

    public void drawLine(float x0, float y0, float x1, float y1, float width, BorderRadius radius, Gradient color) {
        Gradient gradient = mulOpacity(color);
        if (gradient.isFullyTransparent())
            return;
        drawLineInner(
            x0, y0, x1, y1, width, radius,
            gradient.topLeft(), gradient.topRight(), gradient.bottomRight(), gradient.bottomLeft()
        );
    }

    private void drawLineInner(float x0, float y0, float x1, float y1, float width, BorderRadius radius, int colorTL, int colorTR, int colorBR, int colorBL) {
        float dx = x1 - x0;
        float dy = y1 - y0;
        float length = GMath.sqrt(dx * dx + dy * dy);
        if (length == 0)
            return;

        float nx = dx / length;
        float ny = dy / length;
        float cx = (x0 + x1) * 0.5f;
        float cy = (y0 + y1) * 0.5f;

        Matrix3x2f pose = new Matrix3x2f(nx, ny, -ny, nx, cx, cy);
        transform.mul(pose, pose);

        float half = length * 0.5f;
        float halfW = width * 0.5f;

        addCommand(DrawRectCommand.POOL.acquire().set(
            pose,
            -half, -halfW, half, halfW,
            radius,
            colorTL, colorTR, colorBR, colorBL,
            0, 0
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

    public void drawTexture(Sprite sprite, float x, float y, float width, float height, BorderRadius radius,
                            int color) {
        if (ARGB.alpha(color = mulOpacity(color)) == 0)
            return;

        addCommand(DrawTextureCommand.POOL.acquire().set(
            transform,
            x, y, x + width, y + height,
            sprite.u0, sprite.v0, sprite.u1, sprite.v1,
            radius, sprite.textureId, color,
            0, 0, null
        ));
    }

    public void drawTexture(DrawTextureBuilder builder) {
        int outlineColor = mulOpacity(builder.outlineColor);
        boolean hasOutline = ARGB.alpha(outlineColor) != 0 && builder.outlineWidth > 0;
        int color = mulOpacity(builder.color);
        if (ARGB.alpha(color) == 0 && !hasOutline)
            return;

        addCommand(DrawTextureCommand.POOL.acquire().set(
            transform,
            builder.x0, builder.y0, builder.x1, builder.y1,
            builder.u0, builder.v0, builder.u1, builder.v1,
            builder.radius, builder.texture, color,
            outlineColor, builder.outlineWidth,
            builder.nineSlice
        ));
    }

    public void drawText(GigaFont font, String text, float x, float y, float size, int color) {
        if (ARGB.alpha(color = mulOpacity(color)) == 0)
            return;

        addCommand(DrawTextCommand.POOL.acquire().set(
            transform,
            font, text,
            x, y, size,
            color
        ));
    }

    public void drawText(DrawTextBuilder builder) {
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

        addCommand(DrawTextCommand.POOL.acquire().set(
            transform,
            builder.font, builder.text,
            builder.x, builder.y, builder.size,
            color00, color10, color11, color01,
            outlineColor, builder.outlineWidth
        ));
    }

    /**
     * Captures the output of a render function into a sprite that can be drawn as image.
     * <b>Actual rendering of the content will be done in the future.</b>
     */
    public @Nullable Sprite captureSubContext(Consumer<Context> render, float width, float height) {
        float scaleX = transform.m00();
        float scaleY = transform.m11();
        return capturePip(cmd -> {
            Context subContext = new Context();

            AtlasPacker.Rect rect = cmd.sprite.atlasRect();
            subContext.transform.translate(rect.left, rect.top);

            subContext.transform.scale(scaleX, scaleY);
            render.accept(subContext);
            subContext.execute();
        }, width, height);
    }

    public void addPipCommand(Consumer<PipCommand> render, float x, float y, float width, float height) {
        Sprite sprite = capturePip(render, width, height);
        if (sprite == null)
            return;
        addCommand(DrawTextureCommand.POOL.acquire().set(
            transform,
            x, y, x + width, y + height,
            sprite.u0, sprite.v0, sprite.u1, sprite.v1,
            BorderRadius.ZERO, sprite.textureId,
            0xFFFFFFFF,
            0, 0, null
        ));
    }

    /**
     * Captures the output of a render function into a sprite that can be drawn as image.
     * <b>Actual rendering of the content will be done in the future.</b>
     */
    private @Nullable Sprite capturePip(Consumer<PipCommand> render, float width, float height) {
        Vector2f screenSize = pose().transformDirection(width, height, new Vector2f());
        PipAtlasManager.Sprite sprite = PipAtlasManager.insert(
            GMath.ceil(screenSize.x),
            GMath.ceil(screenSize.y)
        );
        if (sprite == null)
            return null;
        pipCommands.add(new PipCommand(render, sprite));
        return new Sprite(
            sprite.textureId(),
            sprite.u0(), sprite.v0(), sprite.u1(), sprite.v1()
        );
    }

    public void addCommand(DrawCommand cmd) {
        Bounds scissor = scissorStack.peekLast();
        if (scissor != null && scissor != UNBOUNDED) {
            cmd.scissor = scissor;
            if (!scissor.intersects(cmd.bounds)) {
                cmd.release();
                return;
            }
            boolean contains = scissor.contains(cmd.bounds);
            if (contains)
                cmd.scissor = null;
            else
                cmd.bounds.intersectionInPlace(scissor);
        }

        priorityCommandMap
            .computeIfAbsent(currentPriority, k -> new ArrayList<>())
            .add(cmd);
    }

    public void execute() {
        if (priorityCommandMap.isEmpty()) {
            reset();
            return;
        }

        if (!pipCommands.isEmpty()) {
            Platform.render().renderPipList(pipCommands);
        }

        // Collect commands in priority order and batch similar commands together
        List<DrawCommand> commands = new ArrayList<>();
        int[] keys = priorityCommandMap.keySet().toArray(new int[0]);
        Arrays.sort(keys);
        for (int key : keys) {
            List<DrawCommand> list = priorityCommandMap.get(key);
            for (DrawCommand cmd : list) {
                addCommandBatching(commands, cmd);
            }
        }

        Platform.render().beforeDraw();

        try {
            int startIndex = 0;
            DrawCommand currentCommand = commands.get(0);
            for (int i = 1; i < commands.size(); i++) {
                DrawCommand cmd = commands.get(i);
                if (cmd.firstInBatch) {
                    executeBatch(currentCommand.getClass(), commands.subList(startIndex, i));
                    startIndex = i;
                    currentCommand = cmd;
                }
            }
            executeBatch(currentCommand.getClass(), commands.subList(startIndex, commands.size()));

        } catch (Exception e) {
            Platform.log().error("Error during draw execution", e);
        }

        Platform.render().stateActiveTexture(GL20.GL_TEXTURE0);
        GL20.glUseProgram(0);

        Platform.render().afterDraw();

        reset();
    }

    @SuppressWarnings("unchecked")
    private static <T extends DrawCommand> void executeBatch(Class<? extends DrawCommand> cmdClass, List<DrawCommand> batch) {
        DrawCommand.Executor<T> executor = (DrawCommand.Executor<T>) EXECUTORS.get(cmdClass);
        if (executor != null) {
            DrawCommand cmd = batch.get(0);
            if (cmd.scissor != null) {
                enableScissor(cmd.scissor, Platform.get().getWindowHeight());
            } else {
                Platform.render().stateDisableScissor();
            }
            try {
                executor.execute((List<T>) batch);
            } catch (GlProgramInvalidException ignored) {
                // Ignore broken program exceptions, they will be logged by the program itself
            } catch (Exception e) {
                Platform.log().error("Error during draw command execution", e);
            }
        }
    }

    private static void addCommandBatching(List<DrawCommand> batch, DrawCommand cmd) {
        // Inspiration
        // https://github.com/nxp-imx/gtec-demo-framework/blob/master/Doc/FslSimpleUI.md#imbatch-flexrendersystem
        // https://github.com/nxp-imx/gtec-demo-framework/blob/e8840f58de1ebd0233391a663d15c4ca8a45d35c/DemoFramework/FslSimpleUI/Render/IMBatch/source/FslSimpleUI/Render/IMBatch/Preprocess/Linear/LinearPreprocessor.hpp#L142
        int maxBacktrack = 16;
        int targetIndex = batch.size() - 1;
        while (targetIndex >= 0 && maxBacktrack-- > 0) {
            DrawCommand prev = batch.get(targetIndex);
            if (!cmd.isSimilar(prev)) {
                if (prev.bounds.intersects(cmd.bounds))
                    break;
                targetIndex -= 1;
                continue;
            }

            // Merge scissors
            cmd.scissor = prev.scissor;

            batch.add(targetIndex + 1, cmd);
            return;
        }

        // no previous task found, just add it to the end
        cmd.firstInBatch = true;
        batch.add(cmd);
    }

    private static void enableScissor(Bounds bounds, float frameHeight) {
        int width = GMath.ceil(bounds.getWidth());
        int height = GMath.ceil(bounds.getHeight());
        int x = GMath.floor(bounds.minX);
        int y = GMath.floor(frameHeight - bounds.maxY);
        Platform.render().stateEnableScissor(x, y, width, height);
    }
}
