package net.glintwein.ui.imui.state;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntStack;
import net.glintwein.ui.GlobalUIState;
import net.glintwein.ui.data.Bounds;
import net.glintwein.ui.imui.ImguiContext;

public class ImguiWindow {
    private final ImguiContext ctx;
    private final String name;
    private final int id;
    private final IntStack idStack = new IntArrayList();
    public float indent;
    public float posX;
    public float posY;
    public float cursorStartX;
    public float cursorStartY;
    public float cursorX;
    public float cursorY;
    public float cursorPosPrevLineX;
    public float cursorPosPrevLineY;
    public float cursorMaxX;
    public float cursorMaxY;
    public float currentLineSizeY;
    public float prevLineSizeY;
    public Bounds clipRect = Bounds.fromXYWH(0, 0, Float.MAX_VALUE, Float.MAX_VALUE);
    public boolean isSameLine;

    public ImguiWindow(ImguiContext ctx, String name) {
        this.ctx = ctx;
        this.name = name;
        idStack.push(0);
        this.id = pushId(name);
    }

    public int getId(String str) {
        int seed = idStack.topInt();
        return hashString(seed, str);
    }

    public int getId(int num) {
        int seed = idStack.topInt();
        return hashInt(seed, num);
    }

    public int pushId(String str) {
        int newId = getId(str);
        idStack.push(newId);
        return newId;
    }

    public int pushId(int id) {
        int newId = getId(id);
        idStack.push(newId);
        return newId;
    }

    public void popId() {
        idStack.popInt();
    }

    public void sameLine() {
        if (isSameLine)
            return;
        isSameLine = true;
        cursorX = cursorPosPrevLineX + ctx.style.itemSpacingX;
        cursorY = cursorPosPrevLineY;
        currentLineSizeY = prevLineSizeY;
    }

    public void layoutItemAdd(float width, float height) {
        float y = isSameLine ? cursorPosPrevLineY : cursorY;
        float lineHeight = Math.max(currentLineSizeY, cursorY - y + height);
        cursorPosPrevLineX = cursorX + width;
        cursorPosPrevLineY = y;
        cursorX = GlobalUIState.snapToPixel(posX + indent); // next line
        cursorY = GlobalUIState.snapToPixel(y + lineHeight + ctx.style.itemSpacingY);
        cursorMaxX = Math.max(cursorMaxX, cursorPosPrevLineX);
        cursorMaxY = Math.max(cursorMaxY, cursorY - ctx.style.itemSpacingY);
        prevLineSizeY = lineHeight;
        currentLineSizeY = 0;
        isSameLine = false;

        float pixelSize = GlobalUIState.minimumOnePixel() * 2;
        ctx.draw.drawRect(cursorMaxX, cursorMaxY, pixelSize, pixelSize, 0xffff00ff);
    }

    public boolean interactiveItemAdd(int id, float x, float y, float width, float height) {
        ctx.lastItemId = id;
        ctx.lastItemBB.set(x, y, width, height);
        boolean visible = clipRect.intersects(ctx.lastItemBB);
        if (!visible && ctx.activeId != id && ctx.activeIdPrevFrame != id)
            return false;
        return true;
    }

    public boolean bbIsVisible(float x, float y, float width, float height) {
        return clipRect.intersects(Bounds.fromXYWH(x, y, width, height));
    }

    private static final int FNV_OFFSET_BASIS = 0x811c9dc5;
    private static final int FNV_PRIME = 0x01000193;

    private static int hashBytes(int seed, byte[] data) {
        int hash = seed == 0 ? FNV_OFFSET_BASIS : seed;
        for (byte b : data) {
            hash ^= (b & 0xff);
            hash *= FNV_PRIME;
        }
        return hash;
    }

    private static int hashString(int seed, String str) {
        int hash = seed == 0 ? FNV_OFFSET_BASIS : seed;
        int len = str.length();
        for (int i = 0; i < len; i++) {
            char c = str.charAt(i);
            hash ^= (c & 0xff);
            hash *= FNV_PRIME;
            hash ^= ((c >> 8) & 0xff);
            hash *= FNV_PRIME;
        }
        return hash;
    }

    private static int hashInt(int seed, int value) {
        int hash = seed == 0 ? FNV_OFFSET_BASIS : seed;
        hash ^= (value & 0xff);
        hash *= FNV_PRIME;
        hash ^= ((value >> 8) & 0xff);
        hash *= FNV_PRIME;
        hash ^= ((value >> 16) & 0xff);
        hash *= FNV_PRIME;
        hash ^= ((value >> 24) & 0xff);
        hash *= FNV_PRIME;
        return hash;
    }
}
