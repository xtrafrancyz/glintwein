package net.glintwein.ui.render.command;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Gui;

import java.util.List;

public class DrawRectCommand extends DrawCommand {
    private final Bounds bounds;
    private final float x, y, width, height;
    private final int color;

    public DrawRectCommand(float x, float y, float width, float height, int color) {
        this.bounds = Bounds.fromXYWH(x, y, width, height);
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.color = color;
    }

    @Override
    public Bounds getBounds() {
        return bounds;
    }

    @Override
    public boolean isSimilar(DrawCommand other) {
        if (!(other instanceof DrawRectCommand)) return false;
        DrawRectCommand o = (DrawRectCommand) other;
        return true; // All rects are similar for batching
    }

    public static class Executor implements DrawCommand.Executor<DrawRectCommand> {
        @Override
        public void execute(List<DrawRectCommand> commands) {
            PoseStack pose = new PoseStack();
            for (DrawRectCommand cmd : commands) {
                Gui.fill(pose,
                    (int) cmd.x,
                    (int) cmd.y,
                    (int) (cmd.x + cmd.width),
                    (int) (cmd.y + cmd.height),
                    cmd.color);
            }
        }
    }
}
