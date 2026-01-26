package net.glintwein.ui.render;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import org.joml.Matrix4f;

public class GlobalRender {
    private static final float[] guiProxMatrix = new float[16];
    private static int projMatrixWidth = -1;
    private static int projMatrixHeight = -1;

    private static final float[] modelViewMatrix = new float[16];

    static {
        new Matrix4f().get(modelViewMatrix);
    }

    public static float[] getGuiProxMatrix() {
        Window window = Minecraft.getInstance().getWindow();
        int width = window.getGuiScaledWidth();
        int height = window.getGuiScaledHeight();
        if (width != projMatrixWidth || height != projMatrixHeight) {
            projMatrixWidth = width;
            projMatrixHeight = height;
            new Matrix4f().setOrtho2D(0, width, height, 0).get(guiProxMatrix);
        }
        return guiProxMatrix;
    }

    public static float[] getModelViewMatrix() {
        return modelViewMatrix;
    }
}
