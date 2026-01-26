package net.glintwein.ui.render.shader;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;

public class GlintVertexFormat {
    public static final VertexFormatElement ELEMENT_RADIUS = new VertexFormatElement(0, VertexFormatElement.Type.UBYTE, VertexFormatElement.Usage.GENERIC, 4);
    public static final VertexFormatElement ELEMENT_SIZE = new VertexFormatElement(0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.GENERIC, 2);

    public static final VertexFormat RECT = new VertexFormat(
        ImmutableList.<VertexFormatElement>builder()
            .add(DefaultVertexFormat.ELEMENT_POSITION)
            .add(DefaultVertexFormat.ELEMENT_COLOR)
            .add(ELEMENT_RADIUS)
            .add(ELEMENT_SIZE)
            .build()
    );

    public static final VertexFormat TEXTURED_RECT = new VertexFormat(
        ImmutableList.<VertexFormatElement>builder()
            .add(DefaultVertexFormat.ELEMENT_POSITION)
            .add(DefaultVertexFormat.ELEMENT_COLOR)
            .add(DefaultVertexFormat.ELEMENT_UV0)
            .add(ELEMENT_RADIUS)
            .add(ELEMENT_SIZE)
            .build()
    );
}
