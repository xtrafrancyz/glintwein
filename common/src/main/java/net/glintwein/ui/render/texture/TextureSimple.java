package net.glintwein.ui.render.texture;

import net.glintwein.platform.GlintImage;
import net.glintwein.platform.Platform;
import net.glintwein.platform.PlatformTexture;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class TextureSimple implements Texture {
    private final Sprite sprite;
    private final int width;
    private final int height;

    public TextureSimple(GlintImage image) {
        this.width = image.getWidth();
        this.height = image.getHeight();
        PlatformTexture texture = Platform.render().createTexture(width, height);
        this.sprite = new Sprite(texture.getGlId(), 0, 0, 1, 1);

        Platform.render().stateBindTexture(sprite.textureId);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        image.upload(texture);
        Platform.render().stateBindTexture(0);
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

    @Override
    public void close() {
        GL11.glDeleteTextures(sprite.textureId);
    }
}
