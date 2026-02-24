package net.glintwein.ui;

import net.glintwein.ui.render.command.Context;
import net.glintwein.ui.test.TestWindow;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

public class WindowManager {
    public List<Window> windows = new ArrayList<>();
    private float screenWidth;
    private float screenHeight;
    private float scale;

    public WindowManager() {
        addWindow(new TestWindow(this));
    }

    public void addWindow(Window window) {
        windows.add(window);
    }

    public void removeWindow(Window window) {
        windows.remove(window);
    }

    public void render() {
        Minecraft mc = Minecraft.getInstance();
        float mouseX = (float) mc.mouseHandler.xpos();
        float mouseY = (float) mc.mouseHandler.ypos();
        boolean mouseGrabbed = mc.mouseHandler.isMouseGrabbed();
        screenWidth = mc.getWindow().getWidth();
        screenHeight = mc.getWindow().getHeight();

        scale = Math.min(screenWidth / 1920f, screenHeight / 1080f);
        if (GlobalUIState.updateYogaPixelScale(scale)) {
            for (Window w : windows) {
                w.invalidateLayout();
            }
        }

        screenWidth /= scale;
        screenHeight /= scale;
        mouseX /= scale;
        mouseY /= scale;

        Context ctx = new Context();
        ctx.pose().scale(scale);
        for (Window w : new ArrayList<>(windows)) {
            w.tick(mouseX, mouseY, !mouseGrabbed);
        }
        for (Window window : windows) {
            window.draw(ctx);
        }
        ctx.execute();
    }

    public boolean onMousePress(float mouseX, float mouseY, int button) {
        mouseX /= scale;
        mouseY /= scale;
        for (int i = windows.size() - 1; i >= 0; i--) {
            Window window = windows.get(i);
            if (window.onMousePress(mouseX, mouseY, button)) {
                // Move to front
                if (i != windows.size() - 1) {
                    windows.remove(i);
                    windows.add(window);
                }
                return true;
            }
        }
        return false;
    }

    public boolean onMouseRelease(float mouseX, float mouseY, int button) {
        mouseX /= scale;
        mouseY /= scale;
        for (int i = windows.size() - 1; i >= 0; i--) {
            Window window = windows.get(i);
            if (window.onMouseRelease(mouseX, mouseY, button)) {
                return true;
            }
        }
        return false;
    }

    public boolean onMouseScroll(float mouseX, float mouseY, float horizontal, float vertical) {
        mouseX /= scale;
        mouseY /= scale;
        for (int i = windows.size() - 1; i >= 0; i--) {
            Window window = windows.get(i);
            if (window.onMouseScroll(mouseX, mouseY, horizontal, vertical)) {
                return true;
            }
        }
        return false;
    }

    public float getScreenWidth() {
        return screenWidth;
    }

    public float getScreenHeight() {
        return screenHeight;
    }

    public float getScale() {
        return scale;
    }
}
