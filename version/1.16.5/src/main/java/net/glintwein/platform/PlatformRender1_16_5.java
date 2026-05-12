package net.glintwein.platform;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.glintwein.ui.data.Bounds;
import net.glintwein.ui.render.command.PipCommand;
import net.glintwein.ui.render.texture.AtlasPacker;
import net.glintwein.ui.util.GMath;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import java.nio.ByteBuffer;
import java.util.PriorityQueue;

public class PlatformRender1_16_5 implements Platform.Render {
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
        GlintRenderTarget firstTarget = commands.peek().sprite.target;

        GlStateManager._matrixMode(GL11.GL_PROJECTION);
        GlStateManager._pushMatrix();
        GlStateManager._loadIdentity();
        GlStateManager._ortho(0.0, firstTarget.getWidth(), firstTarget.getHeight(), 0.0, -3000, 3000.0);
        GlStateManager._matrixMode(GL11.GL_MODELVIEW);
        GlStateManager._pushMatrix();
        GlStateManager._loadIdentity();

        RenderTargetWrapper target = null;
        for (PipCommand cmd : commands) {
            if (cmd.sprite.target != target) {
                target = (RenderTargetWrapper) cmd.sprite.target;
                target.handle.clear(false);
                target.handle.bindWrite(true);
            }

            AtlasPacker.Sprite sprite = cmd.sprite.sprite;
            enableScissor(Bounds.fromMinMax(sprite.left, sprite.top, sprite.right, sprite.bottom), target.getHeight());
            GlStateManager._pushMatrix();
            GlStateManager._translatef(sprite.left, sprite.top, -3000);
            float sx = sprite.right - sprite.left;
            float sy = sprite.bottom - sprite.top;
            GlStateManager._scalef(sx, sy, (sx + sy) / 2f);
            cmd.render.run();
            GlStateManager._popMatrix();
        }


        GlStateManager._matrixMode(GL11.GL_PROJECTION);
        GlStateManager._popMatrix();
        GlStateManager._matrixMode(GL11.GL_MODELVIEW);
        GlStateManager._popMatrix();

        RenderSystem.disableScissor();
        Minecraft.getInstance().getMainRenderTarget().bindWrite(true);
    }

    @Override
    public void beforeDraw() {
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableAlphaTest();
        RenderSystem.disableDepthTest();
    }

    @Override
    public void afterDraw() {
        RenderSystem.disableScissor();
        RenderSystem.enableAlphaTest();
        RenderSystem.enableDepthTest();
    }

    @Override
    public PlatformTexture createTexture(int width, int height) {
        int id = GlStateManager._genTexture();
        GlStateManager._bindTexture(id);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
        return new PlatformTexture1_16_5(id);
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
