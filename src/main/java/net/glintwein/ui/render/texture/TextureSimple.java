package net.glintwein.ui.render.texture;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;

public class TextureSimple implements Texture {
    private final Sprite sprite;
    private final int width;
    private final int height;

    public TextureSimple(NativeImage image) {
        this.width = image.getWidth();
        this.height = image.getHeight();
        this.sprite = new Sprite(GlStateManager._genTexture(), 0, 0, 1, 1);

        TextureUtil.prepareImage(sprite.textureId, width, height);
        image.upload(0, 0, 0, 0, 0, width, height, true, false, false, true);
    }

    @Override
    public Sprite getSprite() {
        return sprite;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }
}
