package net.glintwein;

import com.mojang.blaze3d.opengl.DirectStateAccess;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.opengl.GlTextureView;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.GpuDeviceBackend;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import net.glintwein.mixin.ui.AccessorBackendGlDevice;
import net.glintwein.mixin.ui.AccessorGpuDevice;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;
import org.lwjgl.opengl.GL30;

public class OffscreenHudRenderer {
    public static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("glintwein", "hud_element_texture");

    private static RenderTarget target = null;

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
            Minecraft.getInstance().getTextureManager().register(TEXTURE, new FboTexture(target));
        }

        RenderSystem.getDevice().createCommandEncoder().clearColorTexture(target.getColorTexture(), 0);
        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, getFbo(target));
        GlStateManager._viewport(0, 0, target.width, target.height);
        GlintweinHook.renderLayerIngame();
        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
    }

    public static int getFbo(RenderTarget target) {
        GpuDeviceBackend backend = ((AccessorGpuDevice) RenderSystem.getDevice()).getBackend();
        DirectStateAccess dsa = ((AccessorBackendGlDevice) backend).getDirectStateAccess();
        return ((GlTextureView) target.getColorTextureView()).getFbo(dsa, target.getDepthTexture());
    }

    private static class FboTexture extends AbstractTexture {
        public FboTexture(RenderTarget target) {
            this.texture = target.getColorTexture();
            this.textureView = target.getColorTextureView();
            this.sampler = RenderSystem.getSamplerCache().getRepeat(FilterMode.LINEAR);
        }

        @Override
        public void close() {
            // Do not close the underlying texture, as it is managed by the RenderTarget
        }
    }
}
