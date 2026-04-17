package net.glintwein.ui.render;

import net.glintwein.platform.GlintRenderTarget;
import net.glintwein.platform.Platform;
import net.glintwein.ui.render.texture.AtlasPacker;

import java.util.ArrayList;
import java.util.List;

public class PipAtlasManager {
    private static final List<Atlas> atlases = new ArrayList<>();

    private static int lastWidth = 512;
    private static int lastHeight = 512;

    private static Atlas createAtlas() {
        GlintRenderTarget target = Platform.get().getRender().createRenderTarget(lastWidth, lastHeight, true);
        return new Atlas(target);
    }

    public static void tickStart() {
        int width = Platform.get().getWindowWidth();
        int height = Platform.get().getWindowHeight();
        if (width != lastWidth || height != lastHeight) {
            lastWidth = width;
            lastHeight = height;
            for (Atlas atlas : atlases)
                atlas.target.close();
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
                width, height, atlas.target.getWidth(), atlas.target.getHeight()));
        return new Sprite(atlas.target, sprite);
    }

    public static void reset() {
        for (Atlas atlas : atlases)
            atlas.packer.reset();
    }

    private static class Atlas {
        private final GlintRenderTarget target;
        private final AtlasPacker packer;

        public Atlas(GlintRenderTarget target) {
            this.target = target;
            this.packer = new AtlasPacker(target.getWidth(), target.getHeight());
        }
    }

    public static class Sprite {
        public final GlintRenderTarget target;
        public final AtlasPacker.Sprite sprite;

        public Sprite(GlintRenderTarget target, AtlasPacker.Sprite sprite) {
            this.target = target;
            this.sprite = sprite;

            sprite.v0 = 1 - sprite.v0;
            sprite.v1 = 1 - sprite.v1;
        }
    }
}
