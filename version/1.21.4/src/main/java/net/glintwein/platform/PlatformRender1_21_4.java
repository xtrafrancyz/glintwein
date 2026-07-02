package net.glintwein.platform;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.glintwein.ui.render.command.PipCommand;
import net.glintwein.ui.render.texture.AtlasPacker;
import net.glintwein.ui.util.GMath;
import net.minecraft.client.Minecraft;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.lwjgl.opengl.GL11;

import java.nio.ByteBuffer;
import java.util.PriorityQueue;

public class PlatformRender1_21_4 implements Platform.Render {
    public static boolean isFrameBufferLocked = false;

    public static final Matrix4f projMatrix = new Matrix4f();
    public static final Matrix4f viewMatrix = new Matrix4f();
    public static final Vector3f cameraPos = new Vector3f();

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
        return new RenderTargetWrapper(new TextureTarget(width, height, useDepth));
    }

    @Override
    public void stateEnableScissor(int x, int y, int width, int height) {
        RenderSystem.enableScissor(x, y, width, height);
    }

    @Override
    public void stateDisableScissor() {
        RenderSystem.disableScissor();
    }

    @Override
    public void renderPipList(PriorityQueue<PipCommand> commands) {
        //noinspection DataFlowIssue
        GlintRenderTarget firstTarget = commands.peek().sprite.target();
        boolean previouslyLocked = isFrameBufferLocked;
        isFrameBufferLocked = true;

        // builtin backup and restore can be used in some pips, so need to do it manually
        Matrix4f projMatBackup = new Matrix4f(RenderSystem.getProjectionMatrix());
        RenderSystem.getProjectionMatrix()
            .identity()
            .ortho(0.0f, firstTarget.getWidth(), firstTarget.getHeight(), 0.0f, -3000, 3000.0f);
        RenderSystem.getModelViewStack().pushMatrix();
        RenderSystem.getModelViewStack().identity();

        try {
            RenderTargetWrapper target = null;
            for (PipCommand cmd : commands) {
                if (cmd.sprite.target() != target) {
                    target = (RenderTargetWrapper) cmd.sprite.target();
                    isFrameBufferLocked = false;
                    target.handle.setClearColor(0, 0, 0, 0);
                    target.handle.clear();
                    target.handle.bindWrite(true);
                    target.handle.setFilterMode(GL11.GL_LINEAR);
                    isFrameBufferLocked = true;
                }

                enableScissor(cmd.sprite.atlasRect(), target.getHeight());
                cmd.render.accept(cmd);
            }
        } catch (Exception e) {
            Platform.log().error("Exception while rendering pip list", e);
        }
        isFrameBufferLocked = previouslyLocked;

        RenderSystem.setProjectionMatrix(projMatBackup, ProjectionType.ORTHOGRAPHIC);
        RenderSystem.getModelViewStack().popMatrix();

        RenderSystem.disableScissor();
        Minecraft.getInstance().getMainRenderTarget().bindWrite(true);

        BufferUploader.invalidate();
        GlStateManager._glBindVertexArray(0);
    }

    @Override
    public void beforeDraw() {
        BufferUploader.invalidate();
        GlStateManager._glBindVertexArray(0);
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.disableDepthTest();
    }

    @Override
    public void afterDraw() {
        RenderSystem.disableScissor();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    }

    @Override
    public boolean shouldUseVAO() {
        return true;
    }

    @Override
    public AutoQuadIndexBuffer getQuadAutoIndexBuffer(int indexCount) {
        RenderSystem.AutoStorageIndexBuffer buffer = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
        buffer.bind(indexCount);
        return new AutoQuadIndexBuffer() {
            @Override
            public int getGlId() {
                return -1; // autobind pizdec
            }

            @Override
            public int getGlType() {
                return buffer.type().asGLType;
            }
        };
    }

    @Override
    public PlatformTexture createTexture(int width, int height) {
        int id = GlStateManager._genTexture();
        GlStateManager._bindTexture(id);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
        return new PlatformTexture1_21_4(id);
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
