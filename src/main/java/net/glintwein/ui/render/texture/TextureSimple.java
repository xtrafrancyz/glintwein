package net.glintwein.ui.render.texture;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;

public class TextureSimple implements Texture {
    private final int textureId;
    private final Sprite sprite;

    public TextureSimple(NativeImage image) {
        this.textureId = GlStateManager._genTexture();
        TextureUtil.prepareImage(textureId, image.getWidth(), image.getHeight());
        image.upload(0, 0, 0, 0, 0, image.getWidth(), image.getHeight(), true, false, false, true);
        this.sprite = new Sprite(textureId, 0, 0, 1, 1);
    }

    @Override
    public Sprite getSprite() {
        return sprite;
    }
}
