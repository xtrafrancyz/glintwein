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
        GlintRenderTarget target = Platform.render().createRenderTarget(lastWidth, lastHeight, true);
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
        if (width > lastWidth || height > lastHeight)
            return null;

        for (Atlas atlas : atlases) {
            AtlasPacker.Rect rect = atlas.packer.insert(width, height);
            if (rect != null) {
                atlas.spriteRefCount++;
                return new Sprite(atlas, rect);
            }
        }
        Atlas atlas = createAtlas();
        atlases.add(atlas);
        AtlasPacker.Rect rect = atlas.packer.insert(width, height);
        if (rect == null)
            return null;
        atlas.spriteRefCount++;
        return new Sprite(atlas, rect);
    }

    public static void reset() {
        for (Atlas atlas : atlases)
            atlas.reset();
    }

    private static class Atlas {
        private final GlintRenderTarget target;
        private final AtlasPacker packer;
        private int spriteRefCount = 0;

        public Atlas(GlintRenderTarget target) {
            this.target = target;
            this.packer = new AtlasPacker(target.getWidth(), target.getHeight());
        }

        public void reset() {
            packer.reset();
        }
    }

    public static class Sprite {
        private final Atlas atlas;
        private final AtlasPacker.Rect rect;
        private boolean closed = false;

        private Sprite(Atlas atlas, AtlasPacker.Rect rect) {
            this.atlas = atlas;
            this.rect = rect;

            rect.v0 = 1 - rect.v0;
            rect.v1 = 1 - rect.v1;
        }

        public float u0() {
            return rect.u0;
        }

        public float v0() {
            return rect.v0;
        }

        public float u1() {
            return rect.u1;
        }

        public float v1() {
            return rect.v1;
        }

        public int textureId() {
            return atlas.target.getColorTextureId();
        }

        public AtlasPacker.Rect atlasRect() {
            return rect;
        }

        public GlintRenderTarget target() {
            return atlas.target;
        }

        public net.glintwein.ui.render.texture.Sprite toSprite() {
            return new net.glintwein.ui.render.texture.Sprite(textureId(), u0(), v0(), u1(), v1());
        }

        public void release() {
            if (!closed) {
                closed = true;
                atlas.spriteRefCount--;
                if (atlas.spriteRefCount == 0) {
                    atlas.reset();
                }
            }
        }
    }
}
