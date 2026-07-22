package net.glintwein.ui.element;

import net.glintwein.Glintwein;
import net.glintwein.platform.Platform;
import net.glintwein.ui.GlobalUIState;
import net.glintwein.ui.data.Bounds;
import net.glintwein.ui.render.command.Context;
import net.glintwein.ui.util.ARGB;
import net.glintwein.ui.util.GMath;
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
    private int highlightColor = 0x6633B5E5;

    private String value = "";
    private long blinkTimer;
    private boolean shiftPressed;
    private int cursorPos;
    private int highlightPos;
    private float verticalCursorXCache = -1;
    private float scrollX;
    private boolean hasOverflow;
    private boolean scrollDirty;

    private float lastMouseX, lastMouseY;
    private long lastClickTime;
    private int clickCount;

    public TextInput() {
        setWrapMode(WrapMode.NONE);
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
        // update current text, maybe we are showing placeholder right now
        setText(value);
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
        setWrapMode(multiline ? WrapMode.WORD : WrapMode.NONE);
    }

    public void setAllowRegexp(Pattern allowRegexp) {
        this.allowRegexp = allowRegexp;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    public void setHighlightColor(int color) {
        this.highlightColor = color;
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
        if (mouseX == lastMouseX && mouseY == lastMouseY && Glintwein.time - lastClickTime < 400) {
            clickCount++;
            if (clickCount == 1) {
                int pos = translatePixelToPos(mouseX, mouseY);
                int wordStart = getWordPosition(-1, pos, false);
                int wordEnd = getWordPosition(1, pos, false);
                setCursorPosition(wordEnd);
                setHighlightPosition(wordStart);
            } else if (clickCount == 2) {
                moveCursorToEnd();
                setHighlightPosition(0);
            }
        } else {
            moveCursorTo(translatePixelToPos(mouseX, mouseY));
            clickCount = 0;
        }
        lastMouseX = mouseX;
        lastMouseY = mouseY;
        lastClickTime = Glintwein.time;
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

        boolean ctrl = Platform.input().hasControlDown();
        boolean shift = Platform.input().hasShiftDown();
        boolean alt = Platform.input().hasAltDown();

        if (keyCode == GLFW.GLFW_KEY_A && ctrl && !shift && !alt) {
            this.moveCursorToEnd();
            this.setHighlightPosition(0);
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_C && ctrl && !shift && !alt) {
            Platform.input().setClipboard(this.getHighlighted());
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_V && ctrl && !shift && !alt) {
            if (editable)
                this.insertText(Platform.input().getClipboard());
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_X && ctrl && !shift && !alt) {
            Platform.input().setClipboard(this.getHighlighted());
            if (editable)
                this.insertText("");
            return true;
        } else {
            this.shiftPressed = shift;
            switch (keyCode) {
                case GLFW.GLFW_KEY_BACKSPACE:
                    if (!editable)
                        return true;
                    this.shiftPressed = false;
                    this.deleteText(-1);
                    this.shiftPressed = shift;
                    return true;
                case GLFW.GLFW_KEY_DELETE:
                    if (!editable)
                        return true;
                    this.shiftPressed = false;
                    this.deleteText(1);
                    this.shiftPressed = shift;
                    return true;
                case GLFW.GLFW_KEY_RIGHT:
                    if (ctrl) {
                        this.moveCursorTo(this.getWordPosition(1));
                    } else {
                        this.moveCursor(1);
                    }
                    return true;
                case GLFW.GLFW_KEY_LEFT:
                    if (ctrl) {
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
    public boolean handleCharTyped(char character, int modifiers) {
        if (editable && isAllowedChatCharacter(character)) {
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
        updateScroll(ctx);

        if (hasOverflow) {
            if (!ctx.pushScissor(Bounds.fromBox(contentBox)))
                return;
            ctx.pose().pushMatrix();
            ctx.pose().translate(-scrollX, 0);
        }

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
                    WrappedLine line = getWrappedLines().get(i);
                    float x1 = i == min.y() ? font.getWidth(line.text.substring(0, min.x())) : 0;
                    float x2 = i == max.y() ? font.getWidth(line.text.substring(0, max.x())) : line.width;
                    ctx.drawRect(line.x() + x1, line.y(), x2 - x1, font.getHeight(), highlightColor);
                }
            }

            if ((Glintwein.time - blinkTimer) % 1060L < 530L) {
                WrappedLine line = getWrappedLines().get(cursor.y());
                float cursorOffset = font.getWidth(line.text.substring(0, cursor.x()));
                ctx.drawRect(GMath.ceilX(ctx.pose(), line.x() + cursorOffset), line.y(), ctx.getPixelSize() * 2, font.getHeight(), 0xFFFFFFFF);
            }
        }

        if (hasOverflow) {
            ctx.pose().popMatrix();
            ctx.popScissor();
        }
    }

    @Override
    protected void readYogaLayout() {
        super.readYogaLayout();
        scrollDirty = true;
    }

    protected void onValueChange() {
        scrollDirty = true;
    }

    private void updateScroll(Context ctx) {
        if (!scrollDirty)
            return;
        scrollDirty = false;

        if (multiline) {
            scrollX = 0;
            return;
        }

        WrappedLine line = getWrappedLines().get(0);
        hasOverflow = line.width - contentBox.width > ctx.getPixelSize();
        if (!hasOverflow) {
            scrollX = 0;
            return;
        }
        float cursorOffset = font.getWidth(line.text.substring(0, translatePosToRowCol(cursorPos).x()));
        float minScroll = GMath.clamp(cursorOffset - contentBox.width + ctx.getPixelSize() * 3, 0, cursorOffset);
        float maxScroll = GMath.clamp(line.width - contentBox.width, 0, cursorOffset);
        scrollX = GMath.clamp(scrollX, minScroll, maxScroll);
    }

    private boolean isPlaceholderVisible() {
        return value.isEmpty() && placeholder != null;
    }

    private int translatePixelToPos(float mx, float my) {
        if (isPlaceholderVisible())
            return 0;
        List<WrappedLine> lines = getWrappedLines();
        if (lines.get(0).y() > my)
            return 0;
        float lineHeight = font.getHeight();
        for (int i = 0; i < lines.size(); i++) {
            WrappedLine line = lines.get(i);
            float lineY = line.y();
            if (lineY <= my && my <= lineY + lineHeight) {
                float xRelativeToLine = mx - line.x() + scrollX;
                if (xRelativeToLine <= 0)
                    return translateRowColToPos(i, 0);
                String strBefore = this.font.trimToWidth(line.text, xRelativeToLine);
                return translateRowColToPos(i, strBefore.length());
            }
        }
        return value.length();
    }

    private int translateRowColToPos(int row, int col) {
        if (isPlaceholderVisible())
            return 0;
        List<WrappedLine> lines = getWrappedLines();
        int pos = 0;
        for (int i = 0; i < lines.size(); i++) {
            if (i == row)
                return Math.min(value.length(), pos + col);
            WrappedLine line = lines.get(i);
            pos += line.text.length();
            char c = value.charAt(pos);
            if (c == ' ' || c == '\n')
                pos++;
        }
        return 0;
    }

    private Vector2i translatePosToRowCol(int pos) {
        List<WrappedLine> lines = getWrappedLines();

        // special case for position at the end of text which is exactly on a newline
        if (pos > 0 && pos == value.length())
            return new Vector2i(lines.get(lines.size() - 1).text.length(), lines.size() - 1);

        int posTracker = 0;
        for (int i = 0; i < lines.size(); i++) {
            WrappedLine line = lines.get(i);
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

        String result = new StringBuilder(value).replace(selectionStart, selectionEnd, filtered).toString();
        if (this.allowRegexp == null || this.allowRegexp.matcher(result).matches()) {
            this.setText(result);
            this.setCursorPosition(selectionStart + filteredLen);
            this.setHighlightPosition(this.cursorPos);
            this.onValueChange();
        }
    }

    private void deleteText(int p_94218_) {
        if (Platform.input().hasControlDown()) {
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
                    String s = new StringBuilder(value).delete(j, k).toString();
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
        List<WrappedLine> lines = getWrappedLines();
        Vector2i current = translatePosToRowCol(cursorPos);
        int newRow = GMath.clamp(current.y() + delta, 0, lines.size() - 1);
        if (newRow == current.y())
            return;
        float pixelX;
        if (verticalCursorXCache == -1) {
            WrappedLine line = lines.get(current.y());
            pixelX = line.x() + font.getWidth(line.text.substring(0, current.x()));
            verticalCursorXCache = pixelX;
        } else {
            pixelX = verticalCursorXCache;
        }
        float pixelY = lines.get(newRow).y() + 1;
        int newPos = translatePixelToPos(pixelX, pixelY);
        moveCursorTo(newPos);
    }

    private void moveCursor(int delta) {
        this.moveCursorTo(this.getCursorPos(delta));
    }

    private int getCursorPos(int delta) {
        return offsetByCodepoints(this.value, this.cursorPos, delta);
    }

    private void moveCursorTo(int pos) {
        this.setCursorPosition(pos);
        if (!this.shiftPressed) {
            this.setHighlightPosition(this.cursorPos);
        }
        scrollDirty = true;
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

    // Copy from Util to avoid dependency on net.minecraft.Util
    public static int offsetByCodepoints(String text, int cursorPos, int direction) {
        int i = text.length();
        if (direction >= 0) {
            for (int j = 0; cursorPos < i && j < direction; j++) {
                if (Character.isHighSurrogate(text.charAt(cursorPos++)) && cursorPos < i && Character.isLowSurrogate(text.charAt(cursorPos))) {
                    cursorPos++;
                }
            }
        } else {
            for (int jx = direction; cursorPos > 0 && jx < 0; jx++) {
                cursorPos--;
                if (Character.isLowSurrogate(text.charAt(cursorPos)) && cursorPos > 0 && Character.isHighSurrogate(text.charAt(cursorPos - 1))) {
                    cursorPos--;
                }
            }
        }

        return cursorPos;
    }

    private static String filterText(String input) {
        StringBuilder stringBuilder = new StringBuilder();

        for (char c : input.toCharArray()) {
            if (isAllowedChatCharacter(c)) {
                stringBuilder.append(c);
            } else if (c == '\n') {
                stringBuilder.append(c);
            }
        }

        return stringBuilder.toString();
    }

    // Copy from SharedConstants to avoid dependency on net.minecraft.SharedConstants
    public static boolean isAllowedChatCharacter(char character) {
        return character != 167 && character >= ' ' && character != 127;
    }
}
