package net.glintwein.ui.render.texture;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;

public class PngTexture {
    public final int width;
    public final int height;
    public final int[] rgb;

    private PngTexture(int width, int height, int[] rgb) {
        this.width = width;
        this.height = height;
        this.rgb = rgb;
    }

    public static PngTexture load(InputStream data) {
        BufferedImage image;
        try {
            image = ImageIO.read(data);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load PNG texture", e);
        }
        int width = image.getWidth();
        int height = image.getHeight();
        int[] rgb = new int[width * height];
        image.getRGB(0, 0, width, height, rgb, 0, width);
        return new PngTexture(width, height, rgb);
    }
}
