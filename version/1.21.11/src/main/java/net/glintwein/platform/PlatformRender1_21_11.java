package net.glintwein.platform;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.opengl.GlConst;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.glintwein.OffscreenHudRenderer;
import net.glintwein.mixin.ui.AccessorGlBuffer;
import net.glintwein.ui.render.command.PipCommand;
import net.glintwein.ui.render.texture.AtlasPacker;
import net.glintwein.ui.util.GMath;
import net.minecraft.client.renderer.CachedOrthoProjectionMatrixBuffer;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL33;

import java.util.PriorityQueue;
import java.util.concurrent.ThreadLocalRandom;

public class PlatformRender1_21_11 implements Platform.Render {
    public static final Matrix4f projMatrix = new Matrix4f();
    public static final Matrix4f viewMatrix = new Matrix4f();
    public static final Vector3f cameraPos = new Vector3f();

    private final CachedOrthoProjectionMatrixBuffer projectionMatrixBuffer = new CachedOrthoProjectionMatrixBuffer("glintwein offscreen", -3000.0F, 3000.0F, true);

    @Override
    public Vector3fc getCameraPos() {
        return cameraPos;
    }

    @Override
    public Matrix4fc getWorldProjMatrix() {
        return projMatrix;
    }

    @Override
    public Matrix4fc getWorldViewMatrix() {
        return viewMatrix;
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
        return new RenderTargetWrapper(new TextureTarget("glintwein/fbo", width, height, useDepth));
    }

    @Override
    public void stateEnableScissor(int x, int y, int width, int height) {
        GlStateManager._enableScissorTest();
        GlStateManager._scissorBox(x, y, width, height);
    }

    @Override
    public void stateDisableScissor() {
        GlStateManager._disableScissorTest();
    }

    @Override
    public void renderPipList(PriorityQueue<PipCommand> commands) {
        //noinspection DataFlowIssue
        GlintRenderTarget firstTarget = commands.peek().sprite.target();
        int prevFBO = GlStateManager.getFrameBuffer(GL33.GL_DRAW_FRAMEBUFFER);

        GpuTextureView prevColorTextureOverride = RenderSystem.outputColorTextureOverride;
        GpuTextureView prevDepthTextureOverride = RenderSystem.outputDepthTextureOverride;

        // builtin backup and restore can be used in some pips, so need to do it manually
        GpuBufferSlice prevProjBuffer = RenderSystem.getProjectionMatrixBuffer();
        ProjectionType prevProjType = RenderSystem.getProjectionType();
        RenderSystem.setProjectionMatrix(projectionMatrixBuffer.getBuffer(firstTarget.getWidth(), firstTarget.getHeight()), ProjectionType.ORTHOGRAPHIC);
        RenderSystem.getModelViewStack().pushMatrix();
        RenderSystem.getModelViewStack().identity();

        try {
            RenderTargetWrapper target = null;
            int fbo = -1;
            for (PipCommand cmd : commands) {
                if (cmd.sprite.target() != target) {
                    target = (RenderTargetWrapper) cmd.sprite.target();
                    fbo = OffscreenHudRenderer.getFbo(target.handle);
                    RenderSystem.getDevice().createCommandEncoder().clearColorAndDepthTextures(
                        target.handle.getColorTexture(), 0,
                        target.handle.getDepthTexture(), 1
                    );
                }
                RenderSystem.outputColorTextureOverride = target.handle.getColorTextureView();
                RenderSystem.outputDepthTextureOverride = target.handle.getDepthTextureView();

                GlStateManager._glBindFramebuffer(GL33.GL_DRAW_FRAMEBUFFER, fbo);

                enableScissor(cmd.sprite.atlasRect(), target.getHeight());
                cmd.render.accept(cmd);
            }
        } catch (Exception e) {
            Platform.log().error("Exception while rendering pip list", e);
        }

        RenderSystem.outputColorTextureOverride = prevColorTextureOverride;
        RenderSystem.outputDepthTextureOverride = prevDepthTextureOverride;
        RenderSystem.disableScissorForRenderTypeDraws();
        RenderSystem.setProjectionMatrix(prevProjBuffer, prevProjType);
        RenderSystem.getModelViewStack().popMatrix();

        GlStateManager._glBindFramebuffer(GL33.GL_DRAW_FRAMEBUFFER, prevFBO);

        stateDisableScissor();
        GlStateManager._glBindVertexArray(0);
    }

    @Override
    public void beforeDraw() {
        GlStateManager._glBindVertexArray(0);
        GlStateManager._disableCull();
        GlStateManager._enableBlend();
        GlStateManager._blendFuncSeparate(GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager._disableDepthTest();
        GL33.glBindSampler(0, 0);
    }

    @Override
    public void afterDraw() {
        GlStateManager._disableScissorTest();
        GlStateManager._enableDepthTest();
        GlStateManager._disableBlend();
        GlStateManager._blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }

    @Override
    public boolean shouldUseVAO() {
        return true;
    }

    @Override
    public AutoQuadIndexBuffer getQuadAutoIndexBuffer(int indexCount) {
        RenderSystem.AutoStorageIndexBuffer buffer = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
        GpuBuffer gpuBuffer = buffer.getBuffer(indexCount);
        int glId = ((AccessorGlBuffer) gpuBuffer).getHandle();
        int glType = GlConst.toGl(buffer.type());
        return new AutoQuadIndexBuffer() {
            @Override
            public int getGlId() {
                return glId;
            }

            @Override
            public int getGlType() {
                return glType;
            }
        };
    }

    @Override
    public PlatformTexture createTexture(int width, int height) {
        GpuTexture gpuTexture = RenderSystem.getDevice().createTexture(
            "glintwein/tex" + ThreadLocalRandom.current().nextInt(),
            GpuTexture.USAGE_TEXTURE_BINDING | GpuTexture.USAGE_COPY_DST,
            TextureFormat.RGBA8, width, height,
            1, 1
        );
        return new PlatformTexture1_21_11(gpuTexture);
    }

    private void enableScissor(AtlasPacker.Rect rect, float frameHeight) {
        int width = GMath.ceil((rect.right - rect.left));
        int height = GMath.ceil((rect.bottom - rect.top));
        int x = GMath.floor(rect.left);
        int y = GMath.floor(frameHeight - rect.bottom);
        stateEnableScissor(x, y, width, height);
    }

    private static class RenderTargetWrapper implements GlintRenderTarget {
        final RenderTarget handle;

        public RenderTargetWrapper(RenderTarget handle) {
            this.handle = handle;
        }

        @Override
        public int getColorTextureId() {
            return ((GlTexture) handle.getColorTexture()).glId();
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
