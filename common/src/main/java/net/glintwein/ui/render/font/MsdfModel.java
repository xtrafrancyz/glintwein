package net.glintwein.ui.render.font;

import com.google.gson.annotations.SerializedName;

public class MsdfModel {
    @SerializedName("atlas")
    public Atlas atlas;
    @SerializedName("name")
    public String name;
    @SerializedName("metrics")
    public FontMetrics metrics;
    @SerializedName("glyphs")
    public Glyph[] glyphs;
    @SerializedName("kerning")
    public KerningData[] kerning;

    public static class Atlas {
        @SerializedName("type")
        public String type;
        @SerializedName("distanceRange")
        public float distanceRange;
        @SerializedName("size")
        public float size;
        @SerializedName("width")
        public int width;
        @SerializedName("height")
        public int height;
        @SerializedName("yOrigin")
        public String yOrigin;
    }

    public static class FontMetrics {
        @SerializedName("emSize")
        public float emSize;
        @SerializedName("lineHeight")
        public float lineHeight;
        @SerializedName("ascender")
        public float ascender;
        @SerializedName("descender")
        public float descender;
        @SerializedName("underlineY")
        public float underlineY;
        @SerializedName("underlineThickness")
        public float underlineThickness;
    }

    public static class Glyph {
        @SerializedName("unicode")
        public int unicode;
        @SerializedName("advance")
        public float advance;
        // represents the glyph quad's bounds in em's relative to the baseline and horizontal cursor position.
        @SerializedName("planeBounds")
        public Bounds planeBounds;
        // represents the glyph's bounds in the atlas in pixels.
        @SerializedName("atlasBounds")
        public Bounds atlasBounds;
    }

    public static class Bounds {
        @SerializedName("left")
        public float left;
        @SerializedName("bottom")
        public float bottom;
        @SerializedName("right")
        public float right;
        @SerializedName("top")
        public float top;
    }

    public static class KerningData {
        @SerializedName("unicode1")
        public int first;
        @SerializedName("unicode2")
        public int second;
        @SerializedName("advance")
        public float advance;
    }
}
