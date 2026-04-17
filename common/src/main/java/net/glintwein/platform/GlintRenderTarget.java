package net.glintwein.platform;

public interface GlintRenderTarget {
    int getColorTextureId();

    int getWidth();

    int getHeight();

    void close();
}
