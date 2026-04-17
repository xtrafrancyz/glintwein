package net.glintwein.ui.render.program;

public class GlintVertexFormat {
    public static final GlintVertexFormat MSDF = new GlintVertexFormat(
        GlintVertexFormatElement.POSITION2, // 8
        GlintVertexFormatElement.COLOR, // 4
        GlintVertexFormatElement.UV0 // 8
    ); // sum = 20 bytes

    public static final GlintVertexFormat RECT = new GlintVertexFormat(
        GlintVertexFormatElement.POSITION2, // 8
        GlintVertexFormatElement.COLOR, // 4
        GlintVertexFormatElement.RADIUS, // 4
        GlintVertexFormatElement.SIZE, // 12
        GlintVertexFormatElement.OUTLINE_COLOR // 4
    ); // sum = 32 bytes, quad = 128 bytes

    public static final GlintVertexFormat TEXTURED_RECT = new GlintVertexFormat(
        GlintVertexFormatElement.POSITION2, // 8
        GlintVertexFormatElement.COLOR, // 4
        GlintVertexFormatElement.UV0, // 8
        GlintVertexFormatElement.RADIUS, // 4
        GlintVertexFormatElement.SIZE, // 12
        GlintVertexFormatElement.OUTLINE_COLOR // 4
    );

    GlintVertexFormatElement[] elements;
    private int vertexSize;

    public GlintVertexFormat(GlintVertexFormatElement... elements) {
        this.elements = elements;
        this.vertexSize = 0;
        for (GlintVertexFormatElement element : elements) {
            this.vertexSize += element.type.size * element.count;
        }
    }

    public int getVertexSize() {
        return vertexSize;
    }

    public int getIntegerSize() {
        return vertexSize / 4;
    }
}
