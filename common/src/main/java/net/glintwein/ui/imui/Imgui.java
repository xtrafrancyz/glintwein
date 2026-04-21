package net.glintwein.ui.imui;

import net.glintwein.ui.imui.state.ImguiWindow;
import net.glintwein.ui.render.command.Context;

public class Imgui {
    private static ImguiContext ctx;

    public static void frameStart(Context draw) {
        ctx.newFrame(draw);
    }

    public static void frameEnd() {

    }

    public static void pushStyleColor(IMColor idx, int color) {
        ctx.colorStack.add(new ImguiContext.ModColor(idx.index, ctx.style.colors[idx.index]));
        ctx.style.colors[idx.index] = color;
    }

    public static void popStyleColor() {
        if (ctx.colorStack.isEmpty())
            return;
        ImguiContext.ModColor modColor = ctx.colorStack.remove(ctx.colorStack.size() - 1);
        ctx.style.colors[modColor.index] = modColor.color;
    }

    public static void popStyleColor(int count) {
        for (int i = 0; i < count; i++)
            popStyleColor();
    }

    public static boolean begin(String name) {
        ImguiWindow window = ctx.windows.get(name);
        if (window == null) {
            window = new ImguiWindow(ctx, name);
            ctx.windows.put(name, window);
        }
        ctx.currentWindow = window;

        float contentWidth = window.cursorMaxX - window.cursorStartX;
        float contentHeight = window.cursorMaxY - window.cursorStartY;
        window.cursorMaxX = 0;
        window.cursorMaxY = 0;

        float width = contentWidth + ctx.style.windowPaddingX * 2;
        float height = contentHeight + ctx.style.windowPaddingY * 2;

        window.indent = ctx.style.windowPaddingX;
        window.cursorX = window.posX + ctx.style.windowPaddingX;
        window.cursorY = window.posY + ctx.style.windowPaddingY; // decorations + scroll
        window.cursorStartX = window.cursorX;
        window.cursorStartY = window.cursorY;
        window.cursorPosPrevLineX = window.cursorX;
        window.cursorPosPrevLineY = window.cursorY;

        ctx.draw.drawRect(window.posX, window.posY, width, height, ctx.style.colors[IMColor.WINDOW_BACKGROUND.index]);

        ctx.draw.pushDrawPriority(10);
        return true;
    }

    public static void end() {
        ctx.draw.popDrawPriority(10);
        // draw background
        ctx.currentWindow = null;
    }

    public static void sameLine() {
        ctx.currentWindow.sameLine();
    }

    public static void text(String text) {
        ImguiWindow w = ctx.currentWindow;

        float x = w.cursorX;
        float y = w.cursorY;
        float width = ctx.style.font.getWidth(text, ctx.style.fontSize);
        float height = ctx.style.font.getHeight(ctx.style.fontSize);
        w.layoutItemAdd(width, height);
        if (!w.bbIsVisible(x, y, width, height))
            return;
        ctx.draw.drawText(ctx.style.font, text, x, y, ctx.style.fontSize, ctx.style.colors[IMColor.TEXT.index]);
    }

    public static ImguiContext context() {
        return ctx;
    }

    public static void setContext(ImguiContext ctx) {
        Imgui.ctx = ctx;
    }
}
