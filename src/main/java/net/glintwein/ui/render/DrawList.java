package net.glintwein.ui.render;

import java.nio.ByteBuffer;

public class DrawList {
    private static final float FRINGE = 0.5f;
    private static final int IDX_SIZE = 4;
    private static final int VTX_SIZE = 2 * 4 + 2 * 4 + 4; // pos(2*4) + uv(2*4) + color(4)

    private ByteBuffer idxBuffer = ByteBuffer.allocateDirect(128 * 1024);
    private ByteBuffer vtxBuffer = ByteBuffer.allocateDirect(512 * 1024);
    private int textureId = 0;

    public void addRectFill(float x, float y, float width, float height, int color) {
        reserve(6, 4);
    }

    private void reserve(int idxCount, int vtxCount) {
        if (idxBuffer.remaining() < idxCount * IDX_SIZE) {
            int newSize = idxBuffer.capacity() * 2;
            while (newSize - idxBuffer.position() < idxCount * IDX_SIZE)
                newSize *= 2;

            ByteBuffer newBuffer = ByteBuffer.allocateDirect(newSize);
            idxBuffer.flip();
            newBuffer.put(idxBuffer);
            idxBuffer = newBuffer;
        }

        if (vtxBuffer.remaining() < vtxCount * VTX_SIZE) {
            int newSize = vtxBuffer.capacity() * 2;
            while (newSize - vtxBuffer.position() < vtxCount * VTX_SIZE)
                newSize *= 2;

            ByteBuffer newBuffer = ByteBuffer.allocateDirect(newSize);
            vtxBuffer.flip();
            newBuffer.put(vtxBuffer);
            vtxBuffer = newBuffer;
        }
    }

    private static class RenderCommand {
        int textureId;
        int elemCount;
        int idxOffset;
        int vtxOffset;
    }
}
