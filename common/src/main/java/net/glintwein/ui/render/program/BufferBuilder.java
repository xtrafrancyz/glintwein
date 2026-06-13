package net.glintwein.ui.render.program;

import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

public class BufferBuilder {
    private ByteBuffer buffer;
    private final List<BufferBuilder.DrawState> vertexCounts = new ArrayList<>();
    private int lastRenderedCountIndex = 0;
    private int totalRenderedBytes = 0;
    private int nextElementByte = 0;
    private int totalUploadedBytes = 0;
    private int vertices;
    @Nullable
    private GlintVertexFormatElement currentElement;
    private int elementIndex;
    private int mode;
    private GlintVertexFormat format;
    private boolean building;

    private static ByteBuffer createByteBuffer(int capacity) {
        return ByteBuffer.allocateDirect(capacity).order(ByteOrder.nativeOrder());
    }

    public BufferBuilder(int capacity) {
        this.buffer = createByteBuffer(capacity * 4);
    }

    protected void ensureVertexCapacity() {
        this.ensureCapacity(this.format.getVertexSize());
    }

    private void ensureCapacity(int increaseAmount) {
        if (this.nextElementByte + increaseAmount > this.buffer.capacity()) {
            int i = this.buffer.capacity();
            int j = i + roundUp(increaseAmount);
            ByteBuffer byteBuffer = createByteBuffer(j);
            this.buffer.position(0);
            byteBuffer.put(this.buffer);
            byteBuffer.rewind();
            this.buffer = byteBuffer;
        }
    }

    private static int roundUp(int x) {
        int i = 2097152;
        if (x == 0) {
            return i;
        } else {
            if (x < 0) {
                i *= -1;
            }

            int j = x % i;
            return j == 0 ? x : x + i - j;
        }
    }

    private void limitToVertex(FloatBuffer floatBuffer, int i) {
        int j = this.format.getIntegerSize() * 4;
        floatBuffer.limit(this.totalRenderedBytes / 4 + (i + 1) * j);
        floatBuffer.position(this.totalRenderedBytes / 4 + i * j);
    }

    public State getState() {
        this.buffer.limit(this.nextElementByte);
        this.buffer.position(this.totalRenderedBytes);
        ByteBuffer byteBuffer = ByteBuffer.allocate(this.vertices * this.format.getVertexSize());
        byteBuffer.put(this.buffer);
        this.buffer.clear();
        return new State(byteBuffer, this.format);
    }

    public void restoreState(State state) {
        state.data.clear();
        int i = state.data.capacity();
        this.ensureCapacity(i);
        this.buffer.limit(this.buffer.capacity());
        this.buffer.position(this.totalRenderedBytes);
        this.buffer.put(state.data);
        this.buffer.clear();
        GlintVertexFormat vertexFormat = state.format;
        this.switchFormat(vertexFormat);
        this.vertices = i / vertexFormat.getVertexSize();
        this.nextElementByte = this.totalRenderedBytes + this.vertices * vertexFormat.getVertexSize();
    }

    public void begin(int i, GlintVertexFormat vertexFormat) {
        if (this.building) {
            throw new IllegalStateException("Already building!");
        } else {
            this.building = true;
            this.mode = i;
            this.switchFormat(vertexFormat);
            this.currentElement = vertexFormat.elements[0];
            this.elementIndex = 0;
            this.buffer.clear();
        }
    }

    private void switchFormat(GlintVertexFormat format) {
        if (this.format != format) {
            this.format = format;
        }
    }

    public void end() {
        if (!this.building) {
            throw new IllegalStateException("Not building!");
        } else {
            this.building = false;
            this.vertexCounts.add(new DrawState(this.format, this.vertices, this.mode));
            this.totalRenderedBytes = this.totalRenderedBytes + this.vertices * this.format.getVertexSize();
            this.vertices = 0;
            this.currentElement = null;
            this.elementIndex = 0;
        }
    }

    public void putByte(int index, byte byteValue) {
        this.buffer.put(this.nextElementByte + index, byteValue);
    }

    public void putShort(int index, short shortValue) {
        this.buffer.putShort(this.nextElementByte + index, shortValue);
    }

    public void putFloat(int index, float floatValue) {
        this.buffer.putFloat(this.nextElementByte + index, floatValue);
    }

    public void putInt(int index, int intValue) {
        this.buffer.putInt(this.nextElementByte + index, intValue);
    }

    public void endVertex() {
        if (this.elementIndex != 0) {
            throw new IllegalStateException("Not filled all elements of the vertex");
        } else {
            this.vertices++;
            this.ensureVertexCapacity();
        }
    }

    public void nextElement() {
        GlintVertexFormatElement[] elements = this.format.elements;
        this.elementIndex = (this.elementIndex + 1) % elements.length;
        this.nextElementByte = this.nextElementByte + this.currentElement.getByteSize();
        this.currentElement = elements[this.elementIndex];
        //if (currentElement.getUsage() == VertexFormatElement.Usage.PADDING) {
        //    this.nextElement();
        //}
    }

    public NextBuffer popNextBuffer() {
        DrawState drawState = this.vertexCounts.get(this.lastRenderedCountIndex++);
        this.buffer.position(this.totalUploadedBytes);
        this.totalUploadedBytes = this.totalUploadedBytes + drawState.vertexCount() * drawState.format().getVertexSize();
        this.buffer.limit(this.totalUploadedBytes);
        if (this.lastRenderedCountIndex == this.vertexCounts.size() && this.vertices == 0) {
            this.clear();
        }

        ByteBuffer byteBuffer = this.buffer.slice();
        this.buffer.clear();
        return new NextBuffer(drawState, byteBuffer);
    }

    public void clear() {
        if (this.totalRenderedBytes != this.totalUploadedBytes) {
            //LOGGER.warn("Bytes mismatch " + this.totalRenderedBytes + " " + this.totalUploadedBytes);
        }

        this.discard();
    }

    public void discard() {
        this.totalRenderedBytes = 0;
        this.totalUploadedBytes = 0;
        this.nextElementByte = 0;
        this.vertexCounts.clear();
        this.lastRenderedCountIndex = 0;
    }

    public GlintVertexFormatElement currentElement() {
        if (this.currentElement == null) {
            throw new IllegalStateException("BufferBuilder not started");
        } else {
            return this.currentElement;
        }
    }

    public boolean building() {
        return this.building;
    }

    public static final class DrawState {
        private final GlintVertexFormat format;
        private final int vertexCount;
        private final int mode;

        private DrawState(GlintVertexFormat vertexFormat, int i, int j) {
            this.format = vertexFormat;
            this.vertexCount = i;
            this.mode = j;
        }

        public GlintVertexFormat format() {
            return this.format;
        }

        public int vertexCount() {
            return this.vertexCount;
        }

        public int mode() {
            return this.mode;
        }
    }

    public static class State {
        private final ByteBuffer data;
        private final GlintVertexFormat format;

        private State(ByteBuffer byteBuffer, GlintVertexFormat vertexFormat) {
            this.data = byteBuffer;
            this.format = vertexFormat;
        }
    }

    public static class NextBuffer {
        private final DrawState state;
        private final ByteBuffer buffer;

        public NextBuffer(DrawState state, ByteBuffer buffer) {
            this.state = state;
            this.buffer = buffer;
        }

        public DrawState getState() {
            return state;
        }

        public ByteBuffer getBuffer() {
            return buffer;
        }
    }
}
