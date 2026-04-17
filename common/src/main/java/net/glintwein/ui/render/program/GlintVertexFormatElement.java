package net.glintwein.ui.render.program;

import org.lwjgl.opengl.GL11;

public class GlintVertexFormatElement {
    public static final GlintVertexFormatElement POSITION2 = new GlintVertexFormatElement("Position", Type.FLOAT, 2, false);
    public static final GlintVertexFormatElement RADIUS = new GlintVertexFormatElement("Radius", Type.UBYTE, 4, false);
    public static final GlintVertexFormatElement OUTLINE_COLOR = new GlintVertexFormatElement("OutlineColor", Type.UBYTE, 4, true);
    public static final GlintVertexFormatElement SIZE = new GlintVertexFormatElement("Size", Type.FLOAT, 3, false);
    public static final GlintVertexFormatElement UV0 = new GlintVertexFormatElement("UV0", Type.FLOAT, 2, false);
    public static final GlintVertexFormatElement COLOR = new GlintVertexFormatElement("Color", Type.UBYTE, 4, true);

    final String name;
    final Type type;
    final int count;
    final boolean normalized;

    public GlintVertexFormatElement(String name, Type type, int count, boolean normalized) {
        this.name = name;
        this.type = type;
        this.count = count;
        this.normalized = normalized;
    }

    public int getByteSize() {
        return type.size * count;
    }

    public enum Type {
        FLOAT(4, GL11.GL_FLOAT),
        UBYTE(1, GL11.GL_UNSIGNED_BYTE),
        USHORT(2, GL11.GL_UNSIGNED_SHORT),
        SHORT(2, GL11.GL_SHORT);

        final int size;
        final int glType;

        Type(int size, int glType) {
            this.size = size;
            this.glType = glType;
        }
    }
}
