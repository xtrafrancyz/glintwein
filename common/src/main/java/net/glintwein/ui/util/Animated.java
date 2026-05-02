package net.glintwein.ui.util;

import net.glintwein.Glintwein;

import java.util.concurrent.CompletableFuture;
import java.util.function.IntConsumer;

public abstract class Animated {
    protected long endTime;
    protected float durationMs = 250;
    protected Easing easing = Easing.EASE;
    protected CompletableFuture<End> future;

    public CompletableFuture<End> createFuture() {
        future = new CompletableFuture<>();
        return future;
    }

    public abstract void update();

    public static class End {
        private final long time;

        public End(long time) {
            this.time = time;
        }

        public long getTime() {
            return time;
        }
    }

    public static class Float extends Animated {
        private final FloatConsumer setter;
        private float value;
        private float targetValue;

        public Float(float value) {
            this(null, value);
        }

        public Float(float value, int durationMs, Easing easing) {
            this(null, value);
            this.durationMs = durationMs;
            this.easing = easing;
        }

        public Float(FloatConsumer set, float value) {
            this.setter = set;
            this.value = value;
            this.targetValue = value;
            update();
        }

        public Float(FloatConsumer set, float value, int durationMs, Easing easing) {
            this(set, value);
            this.durationMs = durationMs;
            this.easing = easing;
        }

        public boolean animateIfDifferent(float value) {
            return animateIfDifferent(value, (int) durationMs, easing);
        }

        public boolean animateIfDifferent(float value, int durationMs, Easing easing) {
            if (this.targetValue != value) {
                animate(value, durationMs, easing);
                return true;
            } else {
                return false;
            }
        }

        public void animate(float value) {
            animate(value, (int) durationMs, easing);
        }

        public void animate(float value, int durationMs, Easing easing) {
            this.value = get();
            this.targetValue = value;
            this.durationMs = durationMs;
            this.easing = easing;
            this.endTime = Glintwein.time + durationMs;
            this.future = null;
        }

        public void set(float value) {
            this.endTime = 0;
            this.value = value;
            this.targetValue = value;
            this.future = null;
            update();
        }

        public float get() {
            if (endTime != 0) {
                float t = 1.0f - (endTime - Glintwein.time) / durationMs;
                if (t >= 1.0f) {
                    value = targetValue;
                    endTime = 0;
                    if (future != null) {
                        CompletableFuture<End> _future = future;
                        future = null;
                        _future.complete(new End(endTime));
                    }
                } else {
                    return GMath.lerp(easing.ease(t), value, targetValue);
                }
            }

            return value;
        }

        public float getFinal() {
            return targetValue;
        }

        @Override
        public void update() {
            if (setter != null)
                setter.accept(get());
        }
    }

    public static class Color extends Animated {
        private final IntConsumer setter;
        private int value;
        private int targetValue;

        public Color(int value) {
            this(null, value);
        }

        public Color(int value, int durationMs, Easing easing) {
            this(null, value);
            this.durationMs = durationMs;
            this.easing = easing;
        }

        public Color(IntConsumer set, int value) {
            this.setter = set;
            this.value = value;
            this.targetValue = value;
            update();
        }

        public Color(IntConsumer set, int value, int durationMs, Easing easing) {
            this(set, value);
            this.durationMs = durationMs;
            this.easing = easing;
        }

        public boolean animateIfDifferent(int value) {
            return animateIfDifferent(value, (int) durationMs, easing);
        }

        public boolean animateIfDifferent(int value, int durationMs, Easing easing) {
            if (this.targetValue != value) {
                animate(value, durationMs, easing);
                return true;
            } else {
                return false;
            }
        }

        public void animate(int value) {
            animate(value, (int) durationMs, easing);
        }

        public void animate(int value, int durationMs, Easing easing) {
            this.value = get();
            this.targetValue = value;
            this.durationMs = durationMs;
            this.easing = easing;
            this.endTime = Glintwein.time + durationMs;
            this.future = null;
        }

        public void set(int value) {
            this.endTime = 0;
            this.value = value;
            this.targetValue = value;
            this.future = null;
            update();
        }

        public int get() {
            if (endTime != 0) {
                float t = 1.0f - (endTime - Glintwein.time) / durationMs;
                if (t >= 1.0f) {
                    value = targetValue;
                    endTime = 0;
                    if (future != null) {
                        CompletableFuture<End> _future = future;
                        future = null;
                        _future.complete(new End(endTime));
                    }
                } else {
                    return ARGB.lerp(easing.ease(t), value, targetValue);
                }
            }

            return value;
        }

        public int getFinal() {
            return targetValue;
        }

        @Override
        public void update() {
            if (setter != null)
                setter.accept(get());
        }
    }
}
