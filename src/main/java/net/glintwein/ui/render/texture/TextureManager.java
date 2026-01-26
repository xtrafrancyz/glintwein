package net.glintwein.ui.render.texture;

import com.mojang.blaze3d.platform.NativeImage;
import net.glintwein.util.ResourceLoader;

import java.io.InputStream;

public class TextureManager {
    public static Texture loadTexture(String path) {
        if (path.endsWith(".png")) {
            NativeImage image;
            try (InputStream is = ResourceLoader.getStream(path)) {
                image = NativeImage.read(NativeImage.Format.RGBA, is);
                return new TextureSimple(image);
            } catch (Exception e) {
                throw new RuntimeException("Failed to load texture from path: " + path, e);
            }
        } else {
            throw new UnsupportedOperationException("Unsupported texture format for path: " + path);
        }
    }
}
