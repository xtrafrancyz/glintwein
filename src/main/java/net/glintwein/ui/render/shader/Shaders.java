package net.glintwein.ui.render.shader;

public class Shaders {
    public static GlProgram MSDF = new GlProgram("msdf", GlintVertexFormat.MSDF);
    public static GlProgram RECT = new GlProgram("rect", GlintVertexFormat.RECT);
    public static GlProgram RECT_TEXTURED = new GlProgram("rect_textured", GlintVertexFormat.TEXTURED_RECT);
}
