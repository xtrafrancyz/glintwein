package net.glintwein.platform;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;

public class PlatformImage1_16_5 implements GlintImage {
    private final NativeImage image;

    public PlatformImage1_16_5(NativeImage image) {
        this.image = image;
    }

    @Override
    public int getWidth() {
        return image.getWidth();
    }

    @Override
    public int getHeight() {
        return image.getHeight();
    }

    @Override
    public void upload(int texture) {
        upload(texture, true, false);
    }

    @Override
    public void upload(int texture, boolean blur, boolean clamp) {
        RenderSystem.bindTexture(texture);
        image.upload(0, 0, 0, 0, 0, image.getWidth(), image.getHeight(), blur, clamp, false, false);
    }

    @Override
    public void close() {
        image.close();
    }
}
