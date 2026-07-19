package net.glintwein.ui.render.font;

import it.unimi.dsi.fastutil.chars.Char2CharFunction;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap;
import net.glintwein.platform.GlintImage;
import net.glintwein.platform.Platform;
import net.glintwein.ui.render.program.GlintVertexConsumer;
import net.glintwein.ui.render.texture.TextureSimple;
import net.glintwein.util.ResourceLoaderUtil;
import org.joml.Matrix3x2f;
import org.lwjgl.opengl.GL11;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class GigaFont {
    private static final int CACHE_MAX_ENTRIES = 500;

    private final String name;
    private final MsdfModel.Atlas atlas;
    private final MsdfModel.FontMetrics metrics;
    private final Char2ObjectMap<Glyph> glyphs;
    private final Int2FloatOpenHashMap kerning;
    private final float spaceWidth;
    private int textureId;

    private final Map<String, Float> widthCache = new LinkedHashMap<String, Float>(CACHE_MAX_ENTRIES + 1, .75F, true) {
        public boolean removeEldestEntry(Map.Entry<String, Float> eldest) {
            // Automatically evict the oldest entry when size exceeds capacity
            return size() > CACHE_MAX_ENTRIES;
        }
    };
    private final Function<String, Float> widthCacheLoader = this::calculateWidth;

    private GigaFont(String name, MsdfModel model, GlintImage image) {
        this.name = name;
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

        spaceWidth = getWidth(" ", 1.0f);

        textureId = new TextureSimple(image).getSprite().textureId;
    }

    /**
     * Sets a fallback glyph for a specific character.
     * If the specified 'to' character is not present in the font, this method does nothing.
     * <p>
     * <pre>{@code
     * // Some random 'ᴅ' character is missing in the font,
     * // so we can use regular 'D' as a fallback.
     * .setGlyphFallback('ᴅ', 'D');
     * }</pre>
     */
    public void setGlyphFallback(char from, char to) {
        if (!glyphs.containsKey(to))
            return;
        glyphs.put(from, glyphs.get(to));
    }

    /**
     * Sets a default glyph to be used when a character is not found in the font.
     * If the specified fallbackDefault character is not present in the font, this method does nothing.
     */
    public void setGlyphFallbackDefault(char fallbackDefault) {
        if (!glyphs.containsKey(fallbackDefault))
            return;
        glyphs.defaultReturnValue(glyphs.get(fallbackDefault));
    }

    /**
     * Removes the default glyph fallback, so that characters not found in the font will not be rendered.
     */
    public void removeGlyphFallbackDefault() {
        glyphs.defaultReturnValue(null);
    }

    public String filterUnsupportedCharacters(String text, Char2CharFunction replace) {
        StringBuilder result = null;
        int len = text.length();
        for (int i = 0; i < len; i++) {
            char c = text.charAt(i);
            if (glyphs.containsKey(c)) {
                if (result != null) {
                    result.append(c);
                }
            } else {
                if (result == null) {
                    result = new StringBuilder(text.length());
                    result.append(text, 0, i);
                }
                char replacement = replace.get(c);
                if (glyphs.containsKey(replacement))
                    result.append(replacement);
            }
        }
        if (result == null)
            return text;
        return result.toString();
    }

    private float calculateWidth(String text) {
        float width = 0;
        int len = text.length();
        char prevChar = 0;
        for (int i = 0; i < len; i++) {
            char c = text.charAt(i);
            Glyph glyph = glyphs.get(c);
            if (glyph != null) {
                float kerning = this.kerning.getOrDefault(((prevChar << 16) | c), 0.0f);
                width += glyph.advance + kerning;
                prevChar = c;
            }
        }
        return width;
    }

    public float getSpaceWidth(float size) {
        return spaceWidth * size;
    }

    public float getWidth(String text, float size) {
        float result = widthCache.computeIfAbsent(text, widthCacheLoader);
        return result * size;
    }

    public String trimToWidth(String text, float size, float maxWidth) {
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
                    return text.substring(0, i);
                }
                width += charWidth;
                prevChar = c;
            }
        }
        return text;
    }

    /**
     * Wraps the given text into multiple lines so that each line does not exceed the specified maxWidth. Text can include '\n'
     * characters to indicate explicit line breaks.
     */
    public void wrapText(String text, float size, float maxWidth, List<String> output) {
        String[] lines = text.split("\n", -1);
        for (String line : lines) {
            if (line.isEmpty()) {
                output.add("");
                continue;
            }

            StringBuilder currentLine = new StringBuilder();
            float currentWidth = 0;

            String[] words = line.split(" ", -1);
            for (int i = 0; i < words.length; i++) {
                String word = words[i];
                float wordWidth = getWidth(word, size);
                float spaceWidth = (i == 0) ? 0 : this.spaceWidth * size;

                if (currentWidth + spaceWidth + wordWidth <= maxWidth) {
                    if (i != 0) {
                        currentLine.append(" ");
                        currentWidth += spaceWidth;
                    }
                    currentLine.append(word);
                    currentWidth += wordWidth;
                } else {
                    if (currentLine.length() > 0) {
                        output.add(currentLine.toString());
                    }

                    // If the word itself is longer than maxWidth, we need to split it into smaller parts
                    if (wordWidth > maxWidth) {
                        while (!word.isEmpty()) {
                            String trimmed = trimToWidth(word, size, maxWidth);
                            if (trimmed.isEmpty()) {
                                // If we can't fit any characters, break to avoid infinite loop
                                break;
                            }
                            output.add(trimmed);
                            word = word.substring(trimmed.length());
                            wordWidth = getWidth(word, size);
                            if (wordWidth <= maxWidth) {
                                break;
                            }
                        }
                    }

                    currentLine = new StringBuilder(word);
                    currentWidth = wordWidth;
                }
            }
            if (currentLine.length() > 0) {
                output.add(currentLine.toString());
            }
        }
    }

    public void render(GlintVertexConsumer consumer, Matrix3x2f pose, String text, float x, float y, float size, int color) {
        float cursorX = x;
        float cursorY = y + metrics.ascender * size;
        char prevChar = 0;

        int len = text.length();
        for (int i = 0; i < len; i++) {
            char c = text.charAt(i);
            Glyph glyph = glyphs.get(c);
            if (glyph != null) {
                float kerning = this.kerning.getOrDefault(((prevChar << 16) | c), 0.0f);
                cursorX += kerning * size;
                float x0 = cursorX + glyph.leftPosition * size;
                float y0 = cursorY - glyph.topPosition * size;
                float x1 = x0 + glyph.width * size;
                float y1 = y0 + glyph.height * size;

                consumer.vertex2(pose, x0, y0).color(color).uv(glyph.minU, glyph.maxV).endVertex();
                consumer.vertex2(pose, x0, y1).color(color).uv(glyph.minU, glyph.minV).endVertex();
                consumer.vertex2(pose, x1, y1).color(color).uv(glyph.maxU, glyph.minV).endVertex();
                consumer.vertex2(pose, x1, y0).color(color).uv(glyph.maxU, glyph.maxV).endVertex();

                cursorX += glyph.advance * size;
                prevChar = c;
            }
        }
    }

    public void renderGlyphColor(GlintVertexConsumer consumer, Matrix3x2f pose, String text, float x, float y, float size, int colorTL, int colorTR, int colorBR, int colorBL) {
        float cursorX = x;
        float cursorY = y + metrics.ascender * size;
        char prevChar = 0;

        int len = text.length();
        for (int i = 0; i < len; i++) {
            char c = text.charAt(i);
            Glyph glyph = glyphs.get(c);
            if (glyph != null) {
                float kerning = this.kerning.getOrDefault(((prevChar << 16) | c), 0.0f);
                cursorX += kerning * size;
                float x0 = cursorX + glyph.leftPosition * size;
                float y0 = cursorY - glyph.topPosition * size;
                float x1 = x0 + glyph.width * size;
                float y1 = y0 + glyph.height * size;

                consumer.vertex2(pose, x0, y0).color(colorTL).uv(glyph.minU, glyph.maxV).endVertex();
                consumer.vertex2(pose, x0, y1).color(colorBL).uv(glyph.minU, glyph.minV).endVertex();
                consumer.vertex2(pose, x1, y1).color(colorBR).uv(glyph.maxU, glyph.minV).endVertex();
                consumer.vertex2(pose, x1, y0).color(colorTR).uv(glyph.maxU, glyph.maxV).endVertex();

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

    public String getName() {
        return name;
    }

    public void dispose() {
        if (textureId == -1)
            return;
        GL11.glDeleteTextures(textureId);
        textureId = -1;
    }

    public static GigaFont load(InputStream jsonStream, InputStream imageStream) {
        return load("unnamed", jsonStream, imageStream);
    }

    public static GigaFont load(String name, InputStream jsonStream, InputStream imageStream) {
        MsdfModel model = ResourceLoaderUtil.getJson(jsonStream, MsdfModel.class);

        GlintImage image;
        try {
            image = Platform.get().loadImage(imageStream);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load GigaFont texture", e);
        }
        try {
            return new GigaFont(name, model, image);
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
        final float leftPosition;
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
                this.leftPosition = data.planeBounds.left;
                this.width = data.planeBounds.right - data.planeBounds.left;
                this.height = data.planeBounds.top - data.planeBounds.bottom;
            } else {
                this.topPosition = 0;
                this.leftPosition = 0;
                this.width = 0;
                this.height = 0;
            }
        }
    }
}
