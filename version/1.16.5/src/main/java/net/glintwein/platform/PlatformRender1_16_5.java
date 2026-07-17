package net.glintwein.platform;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.glintwein.RenderMatrixTracker;
import net.glintwein.ui.render.command.PipCommand;
import net.glintwein.ui.render.texture.AtlasPacker;
import net.glintwein.ui.util.GMath;
import net.minecraft.client.Minecraft;
import org.joml.Matrix4fc;
import org.joml.Vector3fc;
import org.lwjgl.opengl.GL11;

import java.nio.ByteBuffer;
import java.util.PriorityQueue;

public class PlatformRender1_16_5 implements Platform.Render {
    @Override
    public Vector3fc getCameraPos() {
        return RenderMatrixTracker.CAMERA_POS;
    }

    @Override
    public Matrix4fc getWorldProjMatrix() {
        return RenderMatrixTracker.PROJ_MATRIX;
    }

    @Override
    public Matrix4fc getWorldViewMatrix() {
        return RenderMatrixTracker.VIEW_MATRIX;
    }

    @Override
    public void stateActiveTexture(int texture) {
        GlStateManager._activeTexture(texture);
    }

    @Override
    public void stateBindTexture(int texture) {
        GlStateManager._bindTexture(texture);
    }

    @Override
    public GlintRenderTarget createRenderTarget(int width, int height, boolean useDepth) {
        return new RenderTargetWrapper(new RenderTarget(width, height, useDepth, false));
    }

    @Override
    public void stateEnableScissor(int x, int y, int width, int height) {
        RenderSystem.enableScissor(x, y, width, height);
    }

    @Override
    public void stateDisableScissor() {
        RenderSystem.disableScissor();
    }

    @SuppressWarnings({"DataFlowIssue", "deprecation"})
    @Override
    public void renderPipList(PriorityQueue<PipCommand> commands) {
        GlintRenderTarget firstTarget = commands.peek().sprite.target();

        // common setup for native rendering
        GlStateManager._matrixMode(GL11.GL_PROJECTION);
        GlStateManager._pushMatrix();
        GlStateManager._loadIdentity();
        GlStateManager._ortho(0.0, firstTarget.getWidth(), firstTarget.getHeight(), 0.0, -3000, 3000.0);
        GlStateManager._matrixMode(GL11.GL_MODELVIEW);
        GlStateManager._pushMatrix();
        GlStateManager._loadIdentity();

        RenderTargetWrapper target = null;
        for (PipCommand cmd : commands) {
            if (cmd.sprite.target() != target) {
                target = (RenderTargetWrapper) cmd.sprite.target();
                target.handle.setClearColor(0, 0, 0, 0);
                target.handle.clear(false);
                target.handle.bindWrite(true);
                if (target.handle.filterMode != GL11.GL_LINEAR)
                    target.handle.setFilterMode(GL11.GL_LINEAR);
            }

            enableScissor(cmd.sprite.atlasRect(), target.getHeight());
            cmd.render.accept(cmd);
        }


        GlStateManager._matrixMode(GL11.GL_PROJECTION);
        GlStateManager._popMatrix();
        GlStateManager._matrixMode(GL11.GL_MODELVIEW);
        GlStateManager._popMatrix();

        RenderSystem.disableScissor();
        Minecraft.getInstance().getMainRenderTarget().bindWrite(true);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void beforeDraw() {
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        // Pre-multiplied alpha blending
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.disableAlphaTest();
        RenderSystem.disableDepthTest();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void afterDraw() {
        RenderSystem.disableScissor();
        RenderSystem.enableAlphaTest();
        RenderSystem.enableDepthTest();
        RenderSystem.defaultBlendFunc();
    }

    @Override
    public PlatformTexture createTexture(int width, int height) {
        int id = GlStateManager._genTexture();
        GlStateManager._bindTexture(id);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
        return new PlatformTexture1_16_5(id);
    }

    private void enableScissor(AtlasPacker.Rect rect, float frameHeight) {
        int width = GMath.ceil((rect.right - rect.left));
        int height = GMath.ceil((rect.bottom - rect.top));
        int x = GMath.floor(rect.left);
        int y = GMath.floor(frameHeight - rect.bottom);
        RenderSystem.enableScissor(x, y, width, height);
    }

    private static class RenderTargetWrapper implements GlintRenderTarget {
        final RenderTarget handle;

        public RenderTargetWrapper(RenderTarget handle) {
            this.handle = handle;
        }

        @Override
        public int getColorTextureId() {
            return handle.getColorTextureId();
        }

        @Override
        public int getWidth() {
            return handle.width;
        }

        @Override
        public int getHeight() {
            return handle.height;
        }

        @Override
        public void close() {
            handle.destroyBuffers();
        }
    }
}
