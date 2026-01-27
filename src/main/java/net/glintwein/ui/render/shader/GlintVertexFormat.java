package net.glintwein.ui.render.shader;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;

public class GlintVertexFormat {
    public static final VertexFormatElement ELEMENT_POSITION2 = new VertexFormatElement(0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.POSITION, 2);
    public static final VertexFormatElement ELEMENT_RADIUS = new VertexFormatElement(0, VertexFormatElement.Type.UBYTE, VertexFormatElement.Usage.GENERIC, 4);
    public static final VertexFormatElement ELEMENT_SIZE = new VertexFormatElement(0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.GENERIC, 2);

    public static final VertexFormat MSDF = new VertexFormat(
        ImmutableList.<VertexFormatElement>builder()
            .add(ELEMENT_POSITION2) // 8
            .add(DefaultVertexFormat.ELEMENT_COLOR) // 4
            .add(DefaultVertexFormat.ELEMENT_UV0) // 8
            .build() // sum = 20 bytes
    );

    public static final VertexFormat RECT = new VertexFormat(
        ImmutableList.<VertexFormatElement>builder()
            .add(ELEMENT_POSITION2) // 8
            .add(DefaultVertexFormat.ELEMENT_COLOR) // 4
            .add(ELEMENT_RADIUS) // 4
            .add(ELEMENT_SIZE) // 8
            .build() // sum = 24 bytes
    );

    public static final VertexFormat TEXTURED_RECT = new VertexFormat(
        ImmutableList.<VertexFormatElement>builder()
            .add(ELEMENT_POSITION2) // 8
            .add(DefaultVertexFormat.ELEMENT_COLOR) // 4
            .add(DefaultVertexFormat.ELEMENT_UV0) // 8
            .add(ELEMENT_RADIUS) // 4
            .add(ELEMENT_SIZE) // 8
            .build() // sum = 32 bytes
    );
}
