package net.glintwein.platform;

public interface GlintImage {
    int getWidth();

    int getHeight();

    void upload(int texture);

    void upload(int texture, boolean blur, boolean clamp);

    void close();
}
