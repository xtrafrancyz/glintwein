package net.glintwein.platform;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;

public class PlatformImage1_21_11 implements GlintImage {
    private final NativeImage image;

    public PlatformImage1_21_11(NativeImage image) {
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
    public void upload(PlatformTexture texture) {
        GpuTexture gpuTexture = ((PlatformTexture1_21_11) texture).gpuTexture;
        RenderSystem.getDevice().createCommandEncoder().writeToTexture(gpuTexture, image);
    }

    @Override
    public void close() {
        image.close();
    }
}
