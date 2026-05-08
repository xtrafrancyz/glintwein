package net.glintwein.ui.render.texture;

import net.glintwein.platform.GlintImage;
import net.glintwein.platform.Platform;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.nio.ByteBuffer;

public class TextureSimple implements Texture {
    private final Sprite sprite;
    private final int width;
    private final int height;

    public TextureSimple(GlintImage image) {
        this.width = image.getWidth();
        this.height = image.getHeight();
        this.sprite = new Sprite(GL11.glGenTextures(), 0, 0, 1, 1);

        Platform.render().stateBindTexture(sprite.textureId);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
        image.upload(sprite.textureId);
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
