package net.glintwein.ui.render.shader;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;

public class Shaders {
    public static GlProgram MSDF = new GlProgram("msdf", DefaultVertexFormat.POSITION_COLOR_TEX);
}
