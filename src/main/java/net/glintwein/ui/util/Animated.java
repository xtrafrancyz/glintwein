package net.glintwein.ui.util;

import net.glintwein.Glintwein;

import java.util.concurrent.CompletableFuture;
import java.util.function.IntConsumer;

public abstract class Animated {
    protected long endTime;
    protected float durationMs;
    protected Easing easing;
    protected CompletableFuture<End> future;

    public abstract void update();

    public static class End {
        private final long time;

        public End(long time) {
            this.time = time;
        }
    }

    public static class Float extends Animated {
        private final FloatConsumer setter;
        private float value;
        private float targetValue;

        public Float(float value) {
            this(null, value);
        }

        public Float(FloatConsumer set, float value) {
            this.setter = set;
            this.value = value;
            this.targetValue = value;
            update();
        }

        public CompletableFuture<End> animateIfDifferent(float value, int durationMs, Easing easing) {
            if (this.targetValue != value) {
                return animate(value, durationMs, easing);
            } else {
                return CompletableFuture.completedFuture(new End(Glintwein.time));
            }
        }

        public CompletableFuture<End> animate(float value, int durationMs, Easing easing) {
            this.value = get();
            this.targetValue = value;
            this.durationMs = durationMs;
            this.easing = easing;
            this.endTime = Glintwein.time + durationMs;
            this.future = new CompletableFuture<>();
            return this.future;
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
                        future.complete(new End(Glintwein.time));
                        future = null;
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

        public Color(IntConsumer set, int value) {
            this.setter = set;
            this.value = value;
            this.targetValue = value;
            update();
        }

        public CompletableFuture<End> animateIfDifferent(int value, int durationMs, Easing easing) {
            if (this.targetValue != value) {
                return animate(value, durationMs, easing);
            } else {
                return CompletableFuture.completedFuture(new End(Glintwein.time));
            }
        }

        public CompletableFuture<End> animate(int value, int durationMs, Easing easing) {
            this.value = get();
            this.targetValue = value;
            this.durationMs = durationMs;
            this.easing = easing;
            this.endTime = Glintwein.time + durationMs;
            this.future = new CompletableFuture<>();
            return this.future;
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
                        future.complete(new End(Glintwein.time));
                        future = null;
                    }
                } else {
                    return ARGB.lerp(easing.ease(t), value, targetValue);
                }
            }

            return value;
        }

        @Override
        public void update() {
            if (setter != null)
                setter.accept(get());
        }
    }
}
