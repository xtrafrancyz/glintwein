package net.glintwein.ui;

import net.glintwein.GlintweinFabricMod;
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

    public void render() {
        float mouseX = GlintweinFabricMod.getMouseX();
        float mouseY = GlintweinFabricMod.getMouseY();
        boolean mouseGrabbed = Minecraft.getInstance().mouseHandler.isMouseGrabbed();
        screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        scale = 1 / (float) Minecraft.getInstance().getWindow().getGuiScale();

        // change scale?

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

    public boolean onMouseScroll(float mouseX, float mouseY, float amount, float vertical) {
        mouseX /= scale;
        mouseY /= scale;
        for (int i = windows.size() - 1; i >= 0; i--) {
            Window window = windows.get(i);
            if (window.onMouseScroll(mouseX, mouseY, amount, vertical)) {
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
}
