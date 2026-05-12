package net.glintwein.platform;

import com.mojang.blaze3d.systems.RenderSystem;

public class PlatformTexture1_21_4 implements PlatformTexture {
    private final int glId;

    public PlatformTexture1_21_4(int glId) {
        this.glId = glId;
    }


    @Override
    public int getGlId() {
        return glId;
    }

    @Override
    public void close() {
        RenderSystem.deleteTexture(glId);
    }
}
