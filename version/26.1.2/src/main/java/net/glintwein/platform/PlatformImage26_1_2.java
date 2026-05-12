package net.glintwein.platform;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;

public class PlatformImage26_1_2 implements GlintImage {
    private final NativeImage image;

    public PlatformImage26_1_2(NativeImage image) {
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
        GpuTexture gpuTexture = ((PlatformTexture26_1_2) texture).gpuTexture;
        RenderSystem.getDevice().createCommandEncoder().writeToTexture(gpuTexture, image);
    }

    @Override
    public void close() {
        image.close();
    }
}
