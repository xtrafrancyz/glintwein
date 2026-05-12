package net.glintwein.platform;

import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.textures.GpuTexture;

public class PlatformTexture26_1_2 implements PlatformTexture {
    public final GpuTexture gpuTexture;

    public PlatformTexture26_1_2(GpuTexture gpuTexture) {
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
