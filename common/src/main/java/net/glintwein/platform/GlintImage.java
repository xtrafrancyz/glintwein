package net.glintwein.platform;

public interface GlintImage {
    int getWidth();

    int getHeight();

    void upload(PlatformTexture texture);

    void close();
}
