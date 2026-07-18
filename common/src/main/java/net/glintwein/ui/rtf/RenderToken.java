package net.glintwein.ui.rtf;

import net.glintwein.ui.render.command.Context;

public interface RenderToken {
    float getWidth();

    float getHeight();

    void draw(Context ctx, float x, float y);

    default RenderToken tryMergeNext(RenderToken next) {
        return null;
    }
}
