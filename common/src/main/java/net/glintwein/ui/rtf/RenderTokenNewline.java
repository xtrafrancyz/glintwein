package net.glintwein.ui.rtf;

import it.unimi.dsi.fastutil.floats.Float2ObjectMap;
import it.unimi.dsi.fastutil.floats.Float2ObjectOpenHashMap;
import net.glintwein.ui.render.command.Context;

import java.util.function.DoubleFunction;

public class RenderTokenNewline implements RenderToken {
    private static final Float2ObjectMap<RenderTokenNewline> CACHE = new Float2ObjectOpenHashMap<>();

    private final float height;

    public RenderTokenNewline(float height) {
        this.height = height;
    }

    @Override
    public float getWidth() {
        return 0;
    }

    @Override
    public float getHeight() {
        return height;
    }

    @Override
    public void draw(Context ctx, float x, float y) {
        // No drawing for newline
    }

    public static RenderTokenNewline get(float height) {
        return CACHE.computeIfAbsent(height, (DoubleFunction<? extends RenderTokenNewline>) (d) -> new RenderTokenNewline((float) d));
    }
}
