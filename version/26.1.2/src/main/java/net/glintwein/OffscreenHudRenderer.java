package net.glintwein;

import com.mojang.blaze3d.GraphicsWorkarounds;
import com.mojang.blaze3d.opengl.DirectStateAccess;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.opengl.GlTextureView;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL30;

import java.util.HashSet;

public class OffscreenHudRenderer {
    private static RenderTarget target = null;
    private static DirectStateAccess dsa = null;

    public static boolean needRender() {
        return Glintwein.instance.layerIngame.hasAnyContent();
    }

    public static void preRender() {
        if (!needRender())
            return;

        Window window = Minecraft.getInstance().getWindow();
        if (target == null || target.width != window.getWidth() || target.height != window.getHeight()) {
            if (target != null) {
                target.destroyBuffers();
            }
            target = new TextureTarget("Glintwein ingame layer", window.getWidth(), window.getHeight(), false);
            if (dsa == null) {
                dsa = DirectStateAccess.create(
                    GL.getCapabilities(),
                    new HashSet<>(),
                    GraphicsWorkarounds.get(RenderSystem.getDevice())
                );
            }
        }

        RenderSystem.getDevice().createCommandEncoder().clearColorTexture(target.getColorTexture(), 0);
        int fbo = ((GlTextureView) target.getColorTextureView()).getFbo(dsa, target.getDepthTexture());
        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, fbo);
        GlStateManager._viewport(0, 0, target.width, target.height);
        GlintweinHook.renderLayerIngame();
        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
    }

    public static GpuTextureView getTextureView() {
        return target.getColorTextureView();
    }
}
