package net.glintwein.platform;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.opengl.GlConst;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.TextureFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.glintwein.mixin.ui.AccessorGlBuffer;
import net.glintwein.ui.ContextExt;
import net.glintwein.ui.data.Bounds;
import net.glintwein.ui.render.command.PipCommand;
import net.glintwein.ui.render.texture.AtlasPacker;
import net.glintwein.ui.util.GMath;
import net.minecraft.client.renderer.Projection;
import net.minecraft.client.renderer.ProjectionMatrixBuffer;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL33;

import java.util.PriorityQueue;
import java.util.concurrent.ThreadLocalRandom;

public class PlatformRender26_1_2 implements Platform.Render {
    public static boolean isFrameBufferLocked = false;

    public static final Matrix4f projMatrix = new Matrix4f();
    public static final Matrix4f viewMatrix = new Matrix4f();
    public static final Vector3f cameraPos = new Vector3f();

    private final ProjectionMatrixBuffer projectionMatrixBuffer = new ProjectionMatrixBuffer("glintwein offscreen");
    private final Projection projection = new Projection();

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
        GlintRenderTarget firstTarget = commands.peek().sprite.target;
        isFrameBufferLocked = true;

        RenderSystem.backupProjectionMatrix();
        projection.setupOrtho(-3000, 3000.0f, firstTarget.getWidth(), firstTarget.getHeight(), true);
        RenderSystem.setProjectionMatrix(projectionMatrixBuffer.getBuffer(projection), ProjectionType.ORTHOGRAPHIC);
        RenderSystem.getModelViewStack().pushMatrix();
        RenderSystem.getModelViewStack().identity();

        try {
            RenderTargetWrapper target = null;
            ContextExt.pose = new PoseStack();
            for (PipCommand cmd : commands) {
                if (cmd.sprite.target != target) {
                    target = (RenderTargetWrapper) cmd.sprite.target;
                    RenderSystem.outputColorTextureOverride = target.handle.getColorTextureView();
                    RenderSystem.outputDepthTextureOverride = target.handle.getDepthTextureView();
                    RenderSystem.getDevice().createCommandEncoder().clearColorAndDepthTextures(
                        target.handle.getColorTexture(), 0,
                        target.handle.getDepthTexture(), 1
                    );
                }

                AtlasPacker.Sprite sprite = cmd.sprite.sprite;
                //RenderSystem.enableScissorForRenderTypeDraws();
                //enableScissor(Bounds.fromMinMax(sprite.left, sprite.top, sprite.right, sprite.bottom), target.getHeight());
                ContextExt.pose.pushPose();
                ContextExt.pose.translate(sprite.left, sprite.top, -3000);
                float sx = sprite.right - sprite.left;
                float sy = sprite.bottom - sprite.top;
                ContextExt.pose.scale(sx, sy, (sx + sy) / 2f);
                cmd.render.run();
                ContextExt.pose.popPose();
            }
        } catch (Exception e) {
            Platform.log().error("Exception while rendering pip list", e);
        }
        RenderSystem.outputColorTextureOverride = null;
        RenderSystem.outputDepthTextureOverride = null;
        RenderSystem.disableScissorForRenderTypeDraws();
        RenderSystem.restoreProjectionMatrix();
        RenderSystem.getModelViewStack().popMatrix();

        stateDisableScissor();
    }

    @Override
    public void beforeDraw() {
        GlStateManager._disableCull();
        GlStateManager._enableBlend();
        GlStateManager._blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager._disableDepthTest();
        GL33.glBindSampler(0, 0);
    }

    @Override
    public void afterDraw() {
        GlStateManager._glBindVertexArray(0);
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
        return new PlatformTexture26_1_2(gpuTexture);
    }

    private void enableScissor(Bounds bounds, float frameHeight) {
        int width = GMath.ceil((bounds.maxX - bounds.minX));
        int height = GMath.ceil((bounds.maxY - bounds.minY));
        int x = GMath.floor(bounds.minX);
        int y = GMath.floor(frameHeight - bounds.maxY);
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
