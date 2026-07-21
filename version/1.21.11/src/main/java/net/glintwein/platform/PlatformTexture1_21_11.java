package net.glintwein.platform;

import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.textures.GpuTexture;

public class PlatformTexture1_21_11 implements PlatformTexture {
    public final GpuTexture gpuTexture;

    public PlatformTexture1_21_11(GpuTexture gpuTexture) {
        this.gpuTexture = gpuTexture;
    }

    @Override
    public int getGlId() {
        return ((GlTexture) gpuTexture).glId();
    }

    @Override
    public void close() {
        gpuTexture.close();
    }
}
