package net.glintwein.ui.element;

import net.glintwein.Glintwein;
import net.glintwein.ui.GlobalUIState;
import net.glintwein.ui.render.command.Context;
import net.glintwein.ui.util.ARGB;
import net.glintwein.ui.util.GMath;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.joml.Vector2i;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.regex.Pattern;

public class TextInput extends Text {
    private Pattern allowRegexp;
    private int maxLength = 1000;
    private boolean multiline;
    private boolean editable = true;
    private String placeholder;
    private Integer placeholderColor;
    private Runnable enterCallback;

    private String value = "";
    private long blinkTimer;
    private boolean shiftPressed;
    private int cursorPos;
    private int highlightPos;
    private float verticalCursorXCache = -1;

    public TextInput() {
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    public void setPlaceholderColor(Integer placeholderColor) {
        this.placeholderColor = placeholderColor;
    }

    public void setEnterHandler(Runnable callback) {
        this.enterCallback = callback;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public void setMultiline(boolean multiline) {
        this.multiline = multiline;
    }

    public void setAllowRegexp(Pattern allowRegexp) {
        this.allowRegexp = allowRegexp;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean isFocusable() {
        return true;
    }

    @Override
    public void handleFocusGain() {
        super.handleFocusGain();
        blinkTimer = Glintwein.time;
    }

    @Override
    public void setText(String text) {
        value = text;

        if (isPlaceholderVisible() && text.isEmpty()) {
            text = placeholder;
        }
        super.setText(text);
    }

    @Override
    protected boolean handleMousePress(float mouseX, float mouseY, int button) {
        super.handleMousePress(mouseX, mouseY, button);
        blinkTimer = Glintwein.time;
        verticalCursorXCache = -1;
        shiftPressed = false;
        this.moveCursorTo(translatePixelToPos(mouseX, mouseY));
        return true;
    }

    @Override
    public boolean handleKeyPress(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            GlobalUIState.clearFocus();
            return true;
        }
        blinkTimer = Glintwein.time;
        float prevVerticalCursorXCache = verticalCursorXCache;
        verticalCursorXCache = -1;
        if (Screen.isSelectAll(keyCode)) {
            this.moveCursorToEnd();
            this.setHighlightPosition(0);
            return true;
        } else if (Screen.isCopy(keyCode)) {
            Minecraft.getInstance().keyboardHandler.setClipboard(this.getHighlighted());
            return true;
        } else if (Screen.isPaste(keyCode)) {
            if (editable)
                this.insertText(Minecraft.getInstance().keyboardHandler.getClipboard());
            return true;
        } else if (Screen.isCut(keyCode)) {
            Minecraft.getInstance().keyboardHandler.setClipboard(this.getHighlighted());
            if (editable)
                this.insertText("");
            return true;
        } else {
            this.shiftPressed = Screen.hasShiftDown();
            switch (keyCode) {
                case GLFW.GLFW_KEY_BACKSPACE:
                    if (!editable)
                        return true;
                    this.shiftPressed = false;
                    this.deleteText(-1);
                    this.shiftPressed = Screen.hasShiftDown();
                    return true;
                case GLFW.GLFW_KEY_DELETE:
                    if (!editable)
                        return true;
                    this.shiftPressed = false;
                    this.deleteText(1);
                    this.shiftPressed = Screen.hasShiftDown();
                    return true;
                case GLFW.GLFW_KEY_RIGHT:
                    if (Screen.hasControlDown()) {
                        this.moveCursorTo(this.getWordPosition(1));
                    } else {
                        this.moveCursor(1);
                    }
                    return true;
                case GLFW.GLFW_KEY_LEFT:
                    if (Screen.hasControlDown()) {
                        this.moveCursorTo(this.getWordPosition(-1));
                    } else {
                        this.moveCursor(-1);
                    }
                    return true;
                case GLFW.GLFW_KEY_HOME:
                    this.moveCursorTo(0);
                    return true;
                case GLFW.GLFW_KEY_END:
                    this.moveCursorToEnd();
                    return true;
                case GLFW.GLFW_KEY_ENTER:
                case GLFW.GLFW_KEY_KP_ENTER:
                    if (!editable)
                        return true;
                    if (enterCallback != null && (!multiline || !shiftPressed)) {
                        enterCallback.run();
                    } else if (this.multiline) {
                        this.insertText("\n");
                    }
                    return true;
                case GLFW.GLFW_KEY_DOWN:
                    verticalCursorXCache = prevVerticalCursorXCache;
                    this.moveCursorVertical(1);
                    return true;
                case GLFW.GLFW_KEY_UP:
                    verticalCursorXCache = prevVerticalCursorXCache;
                    this.moveCursorVertical(-1);
                    return true;
                default:
                    return false;
            }
        }
    }

    @Override
    public boolean handleCharTyped(char character, int keyCode) {
        if (editable && SharedConstants.isAllowedChatCharacter(character)) {
            this.insertText(Character.toString(character));
            return true;
        }
        return false;
    }

    public int getPlaceholderColor() {
        if (placeholderColor != null) {
            return placeholderColor;
        } else {
            return (color & 0x00FFFFFF) | ((ARGB.alpha(color) / 2) << 24);
        }
    }

    @Override
    protected int getTextColor() {
        if (isPlaceholderVisible()) {
            return getPlaceholderColor();
        } else {
            return color;
        }
    }

    @Override
    protected void drawContent(Context ctx) {
        super.drawContent(ctx);

        if (isInFocus()) {
            Vector2i cursor = translatePosToRowCol(cursorPos);

            if (cursorPos != highlightPos) {
                Vector2i highlight = translatePosToRowCol(highlightPos);
                Vector2i min, max;
                if (cursorPos > highlightPos) {
                    min = highlight;
                    max = cursor;
                } else {
                    min = cursor;
                    max = highlight;
                }

                for (int i = min.y(); i <= max.y(); i++) {
                    RenderLine line = getRenderLines().get(i);
                    float x1 = i == min.y() ? font.getWidth(line.text.substring(0, min.x())) : 0;
                    float x2 = i == max.y() ? font.getWidth(line.text.substring(0, max.x())) : line.width;
                    // TODO: fucking blend mode
                    ctx.drawRect(line.x + x1, line.y, x2 - x1, font.getHeight(), 0x6633B5E5);
                }
            }

            if ((Glintwein.time - blinkTimer) % 1060L < 530L) {
                RenderLine line = getRenderLines().get(cursor.y());
                float cursorOffset = font.getWidth(line.text.substring(0, cursor.x()));
                ctx.drawRect(line.x + cursorOffset, line.y, 1, font.getHeight(), 0xFFFFFFFF);
            }
        }
    }

    protected void onValueChange() {

    }

    private boolean isPlaceholderVisible() {
        return value.isEmpty() && placeholder != null;
    }

    private int translatePixelToPos(float mx, float my) {
        List<RenderLine> renderLines = getRenderLines();
        if (renderLines.get(0).y > my)
            return 0;
        float lineHeight = font.getHeight();
        for (int i = 0; i < renderLines.size(); i++) {
            RenderLine line = renderLines.get(i);
            if (line.y <= my && my <= line.y + lineHeight) {
                float xRelativeToLine = mx - line.x;
                if (xRelativeToLine <= 0)
                    return translateRowColToPos(i, 0);
                String strBefore = this.font.trimToWidth(line.text, xRelativeToLine);
                return translateRowColToPos(i, strBefore.length());
            }
        }
        return value.length();
    }

    private int translateRowColToPos(int row, int col) {
        List<RenderLine> renderLines = getRenderLines();
        int pos = 0;
        for (int i = 0; i < renderLines.size(); i++) {
            if (i == row)
                return Math.min(value.length(), pos + col);
            RenderLine line = renderLines.get(i);
            pos += line.text.length();
            char c = value.charAt(pos);
            if (c == ' ' || c == '\n')
                pos++;
        }
        return 0;
    }

    private Vector2i translatePosToRowCol(int pos) {
        List<RenderLine> renderLines = getRenderLines();
        int posTracker = 0;
        for (int i = 0; i < renderLines.size(); i++) {
            RenderLine line = renderLines.get(i);
            if (pos <= line.text.length())
                return new Vector2i(pos, i);

            posTracker += line.text.length();
            pos -= line.text.length();
            char c = value.charAt(posTracker);
            if (c == ' ' || c == '\n') {
                posTracker++;
                pos--;
            }
        }
        return new Vector2i(0, 0);
    }

    private String getHighlighted() {
        int start = Math.min(this.cursorPos, this.highlightPos);
        int end = Math.max(this.cursorPos, this.highlightPos);
        return this.value.substring(start, end);
    }

    private static String filterText(String input) {
        StringBuilder stringBuilder = new StringBuilder();

        for (char c : input.toCharArray()) {
            if (SharedConstants.isAllowedChatCharacter(c)) {
                stringBuilder.append(c);
            } else if (c == '\n') {
                stringBuilder.append(c);
            }
        }

        return stringBuilder.toString();
    }

    private void insertText(String str) {
        int selectionStart = Math.min(this.cursorPos, this.highlightPos);
        int selectionEnd = Math.max(this.cursorPos, this.highlightPos);
        int maxAllowed = this.maxLength - this.value.length() - (selectionStart - selectionEnd);
        String filtered = filterText(str);
        int filteredLen = filtered.length();
        if (maxAllowed < filteredLen) {
            filtered = filtered.substring(0, maxAllowed);
            filteredLen = maxAllowed;
        }

        String result = new StringBuilder(getText()).replace(selectionStart, selectionEnd, filtered).toString();
        if (this.allowRegexp == null || this.allowRegexp.matcher(result).matches()) {
            this.setText(result);
            this.setCursorPosition(selectionStart + filteredLen);
            this.setHighlightPosition(this.cursorPos);
            this.onValueChange();
        }
    }

    private void deleteText(int p_94218_) {
        if (Screen.hasControlDown()) {
            this.deleteWords(p_94218_);
        } else {
            this.deleteChars(p_94218_);
        }
    }

    private void deleteWords(int p_94177_) {
        if (!this.value.isEmpty()) {
            if (this.highlightPos != this.cursorPos) {
                this.insertText("");
            } else {
                this.deleteChars(this.getWordPosition(p_94177_) - this.cursorPos);
            }
        }
    }

    private void deleteChars(int delta) {
        if (!this.value.isEmpty()) {
            if (this.highlightPos != this.cursorPos) {
                this.insertText("");
            } else {
                int i = this.getCursorPos(delta);
                int j = Math.min(i, this.cursorPos);
                int k = Math.max(i, this.cursorPos);
                if (j != k) {
                    String s = new StringBuilder(this.getText()).delete(j, k).toString();
                    if (this.allowRegexp == null || this.allowRegexp.matcher(s).matches()) {
                        this.setText(s);
                        this.moveCursorTo(j);
                        this.onValueChange();
                    }
                }
            }
        }
    }

    private int getWordPosition(int p_94185_) {
        return this.getWordPosition(p_94185_, this.getCursorPosition());
    }

    private int getWordPosition(int p_94129_, int p_94130_) {
        return this.getWordPosition(p_94129_, p_94130_, true);
    }

    private int getWordPosition(int p_94141_, int p_94142_, boolean p_94143_) {
        int i = p_94142_;
        boolean flag = p_94141_ < 0;
        int j = Math.abs(p_94141_);

        for (int k = 0; k < j; ++k) {
            if (!flag) {
                int l = this.value.length();
                i = this.value.indexOf(' ', i);
                if (i == -1) {
                    i = l;
                } else {
                    while (p_94143_ && i < l && this.value.charAt(i) == ' ') {
                        ++i;
                    }
                }
            } else {
                while (p_94143_ && i > 0 && this.value.charAt(i - 1) == ' ') {
                    --i;
                }

                while (i > 0 && this.value.charAt(i - 1) != ' ') {
                    --i;
                }
            }
        }

        return i;
    }

    private void moveCursorVertical(int delta) {
        List<RenderLine> renderLines = getRenderLines();
        Vector2i current = translatePosToRowCol(cursorPos);
        int newRow = GMath.clamp(current.y() + delta, 0, renderLines.size() - 1);
        if (newRow == current.y())
            return;
        float pixelX;
        if (verticalCursorXCache == -1) {
            RenderLine line = renderLines.get(current.y());
            pixelX = line.x + font.getWidth(line.text.substring(0, current.x()));
            verticalCursorXCache = pixelX;
        } else {
            pixelX = verticalCursorXCache;
        }
        float pixelY = renderLines.get(newRow).y + 1;
        int newPos = translatePixelToPos(pixelX, pixelY);
        moveCursorTo(newPos);
    }

    private void moveCursor(int delta) {
        this.moveCursorTo(this.getCursorPos(delta));
    }

    private int getCursorPos(int delta) {
        return Util.offsetByCodepoints(this.value, this.cursorPos, delta);
    }

    private void moveCursorTo(int pos) {
        this.setCursorPosition(pos);
        if (!this.shiftPressed) {
            this.setHighlightPosition(this.cursorPos);
        }
    }

    private void setCursorPosition(int pos) {
        this.cursorPos = GMath.clamp(pos, 0, this.value.length());
    }

    private void setHighlightPosition(int pos) {
        this.highlightPos = GMath.clamp(pos, 0, this.value.length());
    }

    private void moveCursorToEnd() {
        this.moveCursorTo(this.value.length());
    }

    private int getCursorPosition() {
        return this.cursorPos;
    }
}
