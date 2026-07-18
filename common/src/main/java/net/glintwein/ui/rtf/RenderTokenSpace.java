package net.glintwein.ui.rtf;

import it.unimi.dsi.fastutil.floats.Float2ObjectMap;
import it.unimi.dsi.fastutil.floats.Float2ObjectOpenHashMap;
import net.glintwein.ui.render.command.Context;

import java.util.function.DoubleFunction;

public class RenderTokenSpace implements RenderToken {
    private static final Float2ObjectMap<RenderTokenSpace> CACHE = new Float2ObjectOpenHashMap<>();

    private final float width;

    public RenderTokenSpace(float width) {
        this.width = width;
    }

    @Override
    public float getWidth() {
        return width;
    }

    @Override
    public float getHeight() {
        return 0;
    }

    @Override
    public void draw(Context ctx, float x, float y) {

    }

    public static RenderTokenSpace get(float width) {
        return CACHE.computeIfAbsent(width, (DoubleFunction<? extends RenderTokenSpace>) (d) -> new RenderTokenSpace((float) d));
    }
}
