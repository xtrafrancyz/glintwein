package net.glintwein.ui;

import net.glintwein.platform.Platform;
import net.glintwein.ui.element.Element;
import net.glintwein.ui.element.RootElement;
import net.glintwein.ui.render.command.Context;

public class UILayer {
    private final WindowManager windowManager;
    private final RootElement root;

    public UILayer() {
        windowManager = new WindowManager(this);
        root = new RootElement();
    }

    public boolean hasAnyContent() {
        return !windowManager.windows.isEmpty() || !root.getChildren().isEmpty();
    }

    public Element getContent() {
        return root;
    }

    public WindowManager getWindowManager() {
        return windowManager;
    }

    public void invalidateLayout() {
        windowManager.invalidateLayout();
        root.invalidateLayout();
    }

    public void tick() {
        float mouseX = GlobalUIState.getMouseX();
        float mouseY = GlobalUIState.getMouseY();
        boolean canHandleMouse = canHandleMouseInput();
        try {
            windowManager.tick(mouseX, mouseY, canHandleMouse);
            root.updateMouse(mouseX, mouseY, canHandleMouse);
            root.tick();
            root.calculateLayout(GlobalUIState.getScaledWidth(), GlobalUIState.getScaledHeight());
        } catch (Exception e) {
            Platform.log().error("Error while ticking UI layer", e);
        }
    }

    public void render(Context ctx) {
        try {
            ctx.pose().scale(GlobalUIState.getScale());
            windowManager.draw(ctx);
            root.draw(ctx);
            ctx.execute();
        } catch (Exception e) {
            ctx.reset();
            Platform.log().error("Error while rendering UI layer", e);
        }
    }

    protected boolean canHandleMouseInput() {
        return !Platform.input().isMouseGrabbed();
    }

    public boolean onMousePress(float mouseX, float mouseY, int button) {
        if (!canHandleMouseInput())
            return false;
        boolean cancelled = windowManager.onMousePress(mouseX, mouseY, button);
        if (!cancelled)
            cancelled = root.handleMousePress(mouseX, mouseY, button);
        return cancelled;
    }

    public boolean onMouseRelease(float mouseX, float mouseY, int button) {
        boolean cancelled = windowManager.onMouseRelease(mouseX, mouseY, button);
        if (!cancelled)
            cancelled = root.handleMouseRelease(mouseX, mouseY, button);
        return cancelled;
    }

    public boolean onMouseScroll(float mouseX, float mouseY, float horizontal, float vertical) {
        if (!canHandleMouseInput())
            return false;
        boolean cancelled = windowManager.onMouseScroll(mouseX, mouseY, horizontal, vertical);
        if (!cancelled)
            cancelled = root.handleMouseScroll(mouseX, mouseY, horizontal, vertical);
        return cancelled;
    }
}
