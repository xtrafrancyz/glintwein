package net.glintwein.platform;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.glintwein.ui.ContextExt;
import net.glintwein.ui.data.Bounds;
import net.glintwein.ui.render.command.PipCommand;
import net.glintwein.ui.render.texture.AtlasPacker;
import net.glintwein.ui.util.GMath;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.PriorityQueue;

public class PlatformRender1_21_4 implements Platform.Render {
    public static boolean isFrameBufferLocked = false;

    public static Matrix4f projMatrix = new Matrix4f();
    public static Matrix4f viewMatrix = new Matrix4f();

    @Override
    public Vector3f getCameraPos() {
        Vec3 cameraVec3 = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        return new Vector3f((float) cameraVec3.x, (float) cameraVec3.y, (float) cameraVec3.z);
    }

    @Override
    public Matrix4f getWorldProjMatrix() {
        return projMatrix;
    }

    @Override
    public Matrix4f getWorldViewMatrix() {
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
        GlintRenderTarget firstTarget = commands.peek().sprite.target;
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
            ContextExt.pose = new PoseStack();
            for (PipCommand cmd : commands) {
                if (cmd.sprite.target != target) {
                    target = (RenderTargetWrapper) cmd.sprite.target;
                    isFrameBufferLocked = false;
                    target.handle.clear();
                    target.handle.bindWrite(true);
                    isFrameBufferLocked = true;
                }

                AtlasPacker.Sprite sprite = cmd.sprite.sprite;
                enableScissor(Bounds.fromMinMax(sprite.left, sprite.top, sprite.right, sprite.bottom), target.getHeight());
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
        isFrameBufferLocked = false;

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
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
    }

    @Override
    public void afterDraw() {
        RenderSystem.disableScissor();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    @Override
    public boolean shouldUseVAO() {
        return true;
    }

    @Override
    public AutoQuadIndexBuffer getQuadAutoIndexBuffer() {
        RenderSystem.AutoStorageIndexBuffer buffer = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
        return new AutoQuadIndexBuffer() {
            @Override
            public boolean hasCapacity(int indexCount) {
                return buffer.hasStorage(indexCount);
            }

            @Override
            public void bind(int indexCount) {
                buffer.bind(indexCount);
            }

            @Override
            public int getGlType() {
                return buffer.type().asGLType;
            }
        };
    }

    private void enableScissor(Bounds bounds, float frameHeight) {
        int width = GMath.ceil((bounds.maxX - bounds.minX));
        int height = GMath.ceil((bounds.maxY - bounds.minY));
        int x = GMath.floor(bounds.minX);
        int y = GMath.floor(frameHeight - bounds.maxY);
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
