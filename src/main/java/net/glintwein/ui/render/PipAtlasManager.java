package net.glintwein.ui.render;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.Window;
import net.glintwein.ui.render.texture.AtlasPacker;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

public class PipAtlasManager {
    private static final List<Atlas> atlases = new ArrayList<>();

    private static int lastWidth = 512;
    private static int lastHeight = 512;

    private static Atlas createAtlas() {
        RenderTarget target = new RenderTarget(lastWidth, lastHeight, true, false);
        return new Atlas(target);
    }

    public static void tickStart() {
        Window window = Minecraft.getInstance().getWindow();
        int width = window.getWidth();
        int height = window.getHeight();
        if (width != lastWidth || height != lastHeight) {
            lastWidth = width;
            lastHeight = height;
            for (Atlas atlas : atlases)
                atlas.target.destroyBuffers();
            atlases.clear();
        }
    }

    public static Sprite insert(int width, int height) {
        for (Atlas atlas : atlases) {
            AtlasPacker.Sprite sprite = atlas.packer.insert(width, height);
            if (sprite != null)
                return new Sprite(atlas.target, sprite);
        }
        Atlas atlas = createAtlas();
        atlases.add(atlas);
        AtlasPacker.Sprite sprite = atlas.packer.insert(width, height);
        if (sprite == null)
            throw new RuntimeException(String.format("Failed to insert sprite of size %dx%d into atlas of size %dx%d",
                width, height, atlas.target.width, atlas.target.height));
        return new Sprite(atlas.target, sprite);
    }

    public static void reset() {
        for (Atlas atlas : atlases)
            atlas.packer.reset();
    }

    private static class Atlas {
        private final RenderTarget target;
        private final AtlasPacker packer;

        public Atlas(RenderTarget target) {
            this.target = target;
            this.packer = new AtlasPacker(target.width, target.height);
        }
    }

    public static class Sprite {
        public final RenderTarget target;
        public final AtlasPacker.Sprite sprite;

        public Sprite(RenderTarget target, AtlasPacker.Sprite sprite) {
            this.target = target;
            this.sprite = sprite;

            sprite.v0 = 1 - sprite.v0;
            sprite.v1 = 1 - sprite.v1;
        }
    }
}
