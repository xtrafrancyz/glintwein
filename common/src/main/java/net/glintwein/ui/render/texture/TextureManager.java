package net.glintwein.ui.render.texture;

import net.glintwein.platform.GlintImage;
import net.glintwein.platform.Platform;
import net.glintwein.util.ResourceLoader;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class TextureManager {
    public static Texture loadTexture(String path) {
        if (path.endsWith(".png")) {
            GlintImage image;
            try (InputStream is = ResourceLoader.getStream(path)) {
                image = Platform.get().loadImage(is);
                return new TextureSimple(image);
            } catch (Exception e) {
                throw new RuntimeException("Failed to load texture from path: " + path, e);
            }
        } else {
            throw new UnsupportedOperationException("Unsupported texture format for path: " + path);
        }
    }

    public static Texture loadTexture(byte[] data) {
        return loadTexture(new ByteArrayInputStream(data));
    }

    public static Texture loadTexture(InputStream is) {
        try {
            GlintImage image = Platform.get().loadImage(is);
            return new TextureSimple(image);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load texture from input stream", e);
        }
    }
}
