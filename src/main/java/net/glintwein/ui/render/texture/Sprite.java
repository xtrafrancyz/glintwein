package net.glintwein.ui.render.texture;

public class Sprite {
    public int textureId;
    public float u0, v0, u1, v1;

    public Sprite(int textureId, float u0, float v0, float u1, float v1) {
        this.textureId = textureId;
        this.u0 = u0;
        this.v0 = v0;
        this.u1 = u1;
        this.v1 = v1;
    }
}
