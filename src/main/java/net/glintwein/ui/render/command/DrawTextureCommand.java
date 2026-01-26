package net.glintwein.ui.render.command;

public class DrawTextureCommand extends DrawCommand {
    private final int textureId;
    private final float x;
    private final float y;
    private final float width;
    private final float height;
    private final int color;

    private final Bounds bounds;

    public DrawTextureCommand(int textureId, float x, float y, float width, float height, int color) {
        this.textureId = textureId;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.color = color;

        this.bounds = Bounds.fromXYWH(x, y, width, height);
    }

    @Override
    public Bounds getBounds() {
        return bounds;
    }

    @Override
    public boolean isSimilar(DrawCommand other) {
        if (!(other instanceof DrawTextureCommand))
            return false;
        DrawTextureCommand cmd = (DrawTextureCommand) other;
        return this.textureId == cmd.textureId && this.color == cmd.color;
    }
}
