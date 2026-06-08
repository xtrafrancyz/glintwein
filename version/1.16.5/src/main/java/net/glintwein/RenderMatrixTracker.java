package net.glintwein;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;

public class RenderMatrixTracker {
    public static final Matrix4f VIEW_MATRIX = new Matrix4f();
    public static final Matrix4f PROJ_MATRIX = new Matrix4f();
    public static final Vector3f CAMERA_POS = new Vector3f();

    public static void update(Matrix4f view, Matrix4f proj, double camX, double camY, double camZ) {
        VIEW_MATRIX.set(view);
        PROJ_MATRIX.set(proj);
        CAMERA_POS.set(camX, camY, camZ);
    }

    public static Matrix4f toJoml(com.mojang.math.Matrix4f mcMatrix) {
        Matrix4f jomlMatrix = new Matrix4f();
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(16);
            mcMatrix.store(buffer); // Записываем матрицу MC в буфер
            buffer.rewind();        // Сбрасываем позицию буфера в 0 для чтения
            jomlMatrix.set(buffer); // Читаем данные в матрицу JOML
        }
        return jomlMatrix;
    }
}