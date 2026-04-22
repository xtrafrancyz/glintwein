package net.glintwein.platform;

public interface AutoQuadIndexBuffer {
    boolean hasCapacity(int indexCount);

    void bind(int indexCount);

    int getGlType();
}
