package net.glintwein.ui.render.font;

import com.google.gson.Gson;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap;
import net.glintwein.ui.util.ARGB;
import org.lwjgl.opengl.GL11;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.List;

public class GigaFont {
    private static final Gson gson = new Gson();

    private final MsdfModel.Atlas atlas;
    private final MsdfModel.FontMetrics metrics;
    private final Char2ObjectMap<Glyph> glyphs;
    private final Int2FloatOpenHashMap kerning;
    private int textureId;

    private GigaFont(MsdfModel model, NativeImage image) {
        this.atlas = model.atlas;
        this.metrics = model.metrics;

        glyphs = new Char2ObjectOpenHashMap<>();
        for (MsdfModel.Glyph g : model.glyphs)
            glyphs.put((char) g.unicode, new Glyph(g, model.atlas.width, model.atlas.height));

        kerning = new Int2FloatOpenHashMap();
        for (MsdfModel.KerningData k : model.kerning) {
            int key = ((k.first << 16) | k.second);
            kerning.put(key, k.advance);
        }

        textureId = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, image.getWidth(), image.getHeight(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);

        boolean blur = true;
        boolean clamp = true;
        image.upload(0, 0, 0, 0, 0, image.getWidth(), image.getHeight(), blur, clamp, false, true);
    }

    public float getWidth(String text, float size) {
        float width = 0;
        int len = text.length();
        char prevChar = 0;
        for (int i = 0; i < len; i++) {
            char c = text.charAt(i);
            Glyph glyph = glyphs.get(c);
            if (glyph != null) {
                float kerning = this.kerning.getOrDefault(((prevChar << 16) | c), 0.0f);
                width += (glyph.advance + kerning) * size;
                prevChar = c;
            }
        }
        return width;
    }

    public void trimToWidth(String text, float size, float maxWidth, List<String> output) {
        float width = 0;
        int len = text.length();
        char prevChar = 0;
        for (int i = 0; i < len; i++) {
            char c = text.charAt(i);
            Glyph glyph = glyphs.get(c);
            if (glyph != null) {
                float kerning = this.kerning.getOrDefault(((prevChar << 16) | c), 0.0f);
                float charWidth = (glyph.advance + kerning) * size;
                if (width + charWidth > maxWidth) {
                    output.add(text.substring(0, i));
                    trimToWidth(text.substring(i), size, maxWidth, output);
                    return;
                }
                width += charWidth;
                prevChar = c;
            }
        }
        output.add(text);
    }

    public void render(VertexConsumer consumer, String text, float x, float y, float size, int color) {
        float cursorX = x;
        float cursorY = y + metrics.ascender * size;
        char prevChar = 0;
        int a = ARGB.alpha(color);
        int r = ARGB.red(color);
        int g = ARGB.green(color);
        int b = ARGB.blue(color);

        int len = text.length();
        for (int i = 0; i < len; i++) {
            char c = text.charAt(i);
            Glyph glyph = glyphs.get(c);
            if (glyph != null) {
                float kerning = this.kerning.getOrDefault(((prevChar << 16) | c), 0.0f);
                cursorX += kerning * size;
                float x0 = cursorX;
                float y0 = cursorY - glyph.topPosition * size;
                float x1 = x0 + glyph.width * size;
                float y1 = y0 + glyph.height * size;

                consumer.vertex(x0, y0, 0).color(a, r, g, b).uv(glyph.minU, glyph.maxV).endVertex();
                consumer.vertex(x0, y1, 0).color(a, r, g, b).uv(glyph.minU, glyph.minV).endVertex();
                consumer.vertex(x1, y1, 0).color(a, r, g, b).uv(glyph.maxU, glyph.minV).endVertex();
                consumer.vertex(x1, y0, 0).color(a, r, g, b).uv(glyph.maxU, glyph.maxV).endVertex();

                cursorX += glyph.advance * size;
                prevChar = c;
            }
        }
    }

    public float getHeight(float size) {
        return metrics.lineHeight * size;
    }

    public int getTextureId() {
        return textureId;
    }

    public float getPixelRange() {
        return atlas.distanceRange;
    }

    public void dispose() {
        if (textureId == -1)
            return;
        GL11.glDeleteTextures(textureId);
        textureId = -1;
    }

    public static GigaFont load(String path) {
        MsdfModel model;
        try (InputStream is = GigaFont.class.getResourceAsStream(path + ".json")) {
            model = gson.fromJson(new InputStreamReader(is), MsdfModel.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load GigaFont from path: " + path, e);
        }

        NativeImage image;
        try (InputStream is = GigaFont.class.getResourceAsStream(path + ".png")) {
            image = NativeImage.read(NativeImage.Format.RGBA, is);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load GigaFont texture from path: " + path, e);
        }
        try {
            return new GigaFont(model, image);
        } finally {
            image.close();
        }
    }

    private static class Glyph {
        final int code;
        final float minU;
        final float maxU;
        final float minV;
        final float maxV;
        final float advance;
        final float topPosition;
        final float width;
        final float height;

        public Glyph(MsdfModel.Glyph data, int atlasWidth, int atlasHeight) {
            this.code = data.unicode;
            this.advance = data.advance;

            if (data.atlasBounds != null) {
                this.minU = data.atlasBounds.left / atlasWidth;
                this.maxU = data.atlasBounds.right / atlasWidth;
                this.minV = 1 - data.atlasBounds.bottom / atlasHeight;
                this.maxV = 1 - data.atlasBounds.top / atlasHeight;
            } else {
                this.minU = 0;
                this.maxU = 0;
                this.minV = 0;
                this.maxV = 0;
            }

            if (data.planeBounds != null) {
                this.topPosition = data.planeBounds.top;
                this.width = data.planeBounds.right - data.planeBounds.left;
                this.height = data.planeBounds.top - data.planeBounds.bottom;
            } else {
                this.topPosition = 0;
                this.width = 0;
                this.height = 0;
            }
        }
    }
}
