package net.glintwein.ui.element.component;

import net.glintwein.ui.data.*;
import net.glintwein.ui.element.Element;
import net.glintwein.ui.element.Text;
import net.glintwein.ui.element.TextInput;
import net.glintwein.ui.render.command.Context;
import net.glintwein.ui.render.font.Fonts;
import net.glintwein.ui.util.ARGB;
import net.glintwein.ui.util.GMath;

import java.awt.*;

public class ColorPicker extends Element {
    private final SVBox svBox;
    private final HueSlider hueSlider;

    private final float[] hsb = new float[3]; // h, s, b (0.0 to 1.0)

    public ColorPicker(String name, int initialColor) {
        updateFromColor(initialColor);

        setBorderRadius(new BorderRadius(5));
        setBackground(0xFF1A142E);
        setPadding(Edge.ALL, 5);

        addChild(new Text(name));
        addChild(svBox = new SVBox());
        addChild(hueSlider = new HueSlider());
        addChild(new PreviewBox());
    }

    public void updateFromColor(int argb) {
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;
        Color.RGBtoHSB(r, g, b, hsb);
    }

    public int getSelectedColor() {
        return ARGB.setAlpha(Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]), 255);
    }

    private class SVBox extends Element {
        public SVBox() {
            setSize(200, 100);
            setBorderRadius(new BorderRadius(5));
            setMargin(Edge.VERTICAL, 10);
            setMousePressHandler((x, y, button) -> {
                setByMouse(x + borderBox.x, y + borderBox.y);
                return true; // Consume clicks to prevent them from propagating to the parent
            });
        }

        private int getPureHueRGB() {
            return ARGB.setAlpha(Color.HSBtoRGB(hsb[0], 1.0f, 1.0f), 255);
        }

        @Override
        protected void handleMouseMoved(float mouseX, float mouseY, boolean canHover) {
            super.handleMouseMoved(mouseX, mouseY, canHover);
            if (isPressed())
                setByMouse(mouseX, mouseY);
        }

        private void setByMouse(float mouseX, float mouseY) {
            float relativeX = (mouseX - contentBox.x) / contentBox.width;
            float relativeY = (mouseY - contentBox.y) / contentBox.height;
            hsb[1] = GMath.clamp(relativeX, 0, 1); // Saturation
            hsb[2] = GMath.clamp(1 - relativeY, 0, 1); // Value (inverted Y)
        }

        @Override
        public void draw(Context ctx) {
            // 1. Draw the Base: White -> Selected Hue (Horizontal)
            // Left side is White (0% Sat), Right side is the Pure Color (100% Sat)
            ctx.drawRect(contentBox, borderRadius, Gradient.leftToRight(0xFFFFFFFF, getPureHueRGB()));

            // 2. Draw the Value Overlay: Transparent -> Black (Vertical)
            // Top is 0% Black (100% Value), Bottom is 100% Black (0% Value)
            ctx.drawRect(contentBox.copy().expand(0.1f), borderRadius, Gradient.topToBottom(0x00000000, 0xFF000000));

            // 3. Cursor
            float cursorX = contentBox.x + (hsb[1] * contentBox.width);
            float cursorY = contentBox.y + ((1.0f - hsb[2]) * contentBox.height);
            // Draw white ring for cursor
            ctx.drawRect(cursorX - 3, cursorY - 3, 6, 6, new BorderRadius(3), 0xFFFFFFFF);
        }
    }

    private class HueSlider extends Element {
        public HueSlider() {
            setSize(200, 10);
            setBorderRadius(new BorderRadius(5));
            setMousePressHandler((x, y, button) -> {
                setByMouse(x + borderBox.x);
                return true; // Consume clicks to prevent them from propagating to the parent
            });
        }

        @Override
        protected void handleMouseMoved(float mouseX, float mouseY, boolean canHover) {
            super.handleMouseMoved(mouseX, mouseY, canHover);
            if (isPressed())
                setByMouse(mouseX);
        }

        private void setByMouse(float mouseX) {
            float relativeX = (mouseX - contentBox.x) / contentBox.width;
            hsb[0] = GMath.clamp(relativeX, 0, 1); // Hue
        }

        @Override
        public void draw(Context ctx) {
            // Draw a horizontal gradient representing the hue spectrum
            // Red -> Yellow -> Green -> Cyan -> Blue -> Magenta -> Red
            // segmented into 6 parts, each representing a transition between two primary/secondary colors
            int[] hueColors = new int[]{
                0xFFFF0000, // Red
                0xFFFFFF00, // Yellow
                0xFF00FF00, // Green
                0xFF00FFFF, // Cyan
                0xFF0000FF, // Blue
                0xFFFF00FF, // Magenta
                0xFFFF0000  // Red again to complete the loop
            };

            float width = contentBox.width / 6f;
            for (int i = 0; i < hueColors.length - 1; i++) {
                BorderRadius radius = BorderRadius.ZERO;
                if (i == 0)
                    radius = new BorderRadius().left(5);
                else if (i == hueColors.length - 2)
                    radius = new BorderRadius().right(5);
                ctx.drawRect(contentBox.x + width * i, contentBox.y, width, contentBox.height, radius, Gradient.leftToRight(hueColors[i], hueColors[i + 1]));
            }

            // Hue Cursor (Current H position)
            float thumbX = contentBox.x + (hsb[0] * contentBox.width);
            ctx.drawRect(thumbX - 2, contentBox.y - 1, 4, contentBox.height + 2, new BorderRadius(2), 0xFFFFFFFF);
        }
    }

    private class PreviewBox extends Element {
        private final Element colorBox;
        private final Text hex;

        public PreviewBox() {
            setBorderRadius(new BorderRadius(5));
            setMargin(Edge.TOP, 10);
            setPadding(Edge.ALL, 5);
            setBackground(0xff000000);
            setFlexDirection(FlexDirection.ROW);

            addChild(colorBox = new Element());
            colorBox.setSize(20);
            colorBox.setBorderRadius(new BorderRadius(3));

            addChild(hex = new Input());
            hex.setMargin(Edge.LEFT, 3);

            Element spacer = new Element();
            spacer.setFlexGrow(1);
            addChild(spacer);

            Text hexLabel = new Text("HEX");
            hexLabel.setColor(0xFFAAAAAA);
            hexLabel.setFont(Fonts.REGULAR, 12);
            hexLabel.setAlignSelf(Align.CENTER);
            addChild(hexLabel);
        }

        @Override
        public void tick() {
            super.tick();
            int color = getSelectedColor();
            colorBox.setBackground(color);
        }

        @Override
        public void draw(Context ctx) {
            super.draw(ctx);
        }
    }

    private class Input extends TextInput {
        public Input() {
        }

        @Override
        protected void onValueChange() {
            super.onValueChange();
            String hex = getValue();
            int color = parseHex(hex);
            updateFromColor(color);
        }

        private String toHex(int argb) {
            return String.format("#%06X", (argb & 0xFFFFFF));
        }

        private int parseHex(String hex) {
            try {
                if (hex.startsWith("#"))
                    hex = hex.substring(1);
                int rgb = Integer.parseInt(hex, 16);
                return ARGB.setAlpha(rgb, 255);
            } catch (NumberFormatException e) {
                return getSelectedColor(); // Return current color if parsing fails
            }
        }

        @Override
        public void tick() {
            super.tick();
            if (!isInFocus())
                setText(toHex(getSelectedColor()));
        }
    }
}
