package net.glintwein.ui.util;

public interface Easing {
    float ease(float t);

    Easing LINEAR = t -> t;
    Easing IN_SINE = t -> GMath.sin(t * GMath.HALF_PI);
    Easing OUT_SINE = t -> 1 + GMath.sin((t - 1) * GMath.PI / 2);
    Easing IN_OUT_SINE = t -> 0.5f * (1 + GMath.sin(GMath.PI * (t - 0.5f)));
    Easing IN_QUAD = t -> t * t;
    Easing OUT_QUAD = t -> t * (2 - t);
    Easing IN_OUT_QUAD = t -> t < .5 ? 2 * t * t : -1 + (4 - 2 * t) * t;
    Easing IN_CUBIC = t -> t * t * t;
    Easing OUT_CUBIC = t -> (--t) * t * t + 1;
    Easing IN_OUT_CUBIC = t -> t < .5 ? 4 * t * t * t : (t - 1) * (2 * t - 2) * (2 * t - 2) + 1;
    Easing IN_QUART = t -> t * t * t * t;
    Easing OUT_QUART = t -> 1 - (--t) * t * t * t;
    Easing IN_OUT_QUART = t -> t < .5 ? 8 * t * t * t * t : 1 - 8 * (--t) * t * t * t;
    Easing IN_QUINT = t -> t * t * t * t * t;
    Easing OUT_QUINT = t -> 1 + (--t) * t * t * t * t;
    Easing IN_OUT_QUINT = t -> t < .5 ? 16 * t * t * t * t * t : 1 + 16 * (--t) * t * t * t * t;
    Easing IN_EXPO = t -> ((float) Math.pow(2, 8 * t) - 1) / 255;
    Easing OUT_EXPO = t -> 1 - (float) Math.pow(2, -8 * t);
    Easing IN_OUT_EXPO = t -> t < 0.5f ? ((float) Math.pow(2, 16 * t) - 1) / 510 : 1 - 0.5f * (float) Math.pow(2, -16 * (t - 0.5f));
    Easing IN_CIRC = t -> 1 - GMath.sqrt(1 - t);
    Easing OUT_CIRC = value -> GMath.sqrt(value);
    Easing IN_OUT_CIRC = t -> t < 0.5f ? (1 - GMath.sqrt(1 - 2 * t)) * 0.5f : (1 + GMath.sqrt(2 * t - 1)) * 0.5f;
    Easing IN_BACK = t -> t * t * (2.70158f * t - 1.70158f);
    Easing OUT_BACK = t -> 1 + (--t) * t * (2.70158f * t + 1.70158f);
    Easing IN_OUT_BACK = t -> t < 0.5f ? t * t * (7 * t - 2.5f) * 2 : 1 + (--t) * t * 2 * (7 * t + 2.5f);
    Easing IN_ELASTIC = t -> {
        float t2 = t * t;
        return t2 * t2 * GMath.sin(t * GMath.PI * 4.5f);
    };
    Easing OUT_ELASTIC = t -> {
        float t2 = (t - 1) * (t - 1);
        return 1 - t2 * t2 * GMath.cos(t * GMath.PI * 4.5f);
    };
    Easing IN_OUT_ELASTIC = t -> {
        float t2;
        if (t < 0.45f) {
            t2 = t * t;
            return 8 * t2 * t2 * GMath.sin(t * GMath.PI * 9);
        } else if (t < 0.55f) {
            return 0.5f + 0.75f * GMath.sin(t * GMath.PI * 4);
        } else {
            t2 = (t - 1) * (t - 1);
            return 1 - 8 * t2 * t2 * GMath.sin(t * GMath.PI * 9);
        }
    };
    Easing IN_BOUNCE = t -> (float) Math.pow(2, 6 * (t - 1)) * GMath.abs(GMath.sin(t * GMath.PI * 3.5f));
    Easing OUT_BOUNCE = t -> 1 - (float) Math.pow(2, -6 * t) * GMath.abs(GMath.cos(t * GMath.PI * 3.5f));
    Easing IN_OUT_BOUNCE = t -> {
        if (t < 0.5) {
            return 8 * (float) Math.pow(2, 8 * (t - 1)) * GMath.abs(GMath.sin(t * GMath.PI * 7));
        } else {
            return 1 - 8 * (float) Math.pow(2, -8 * t) * GMath.abs(GMath.sin(t * GMath.PI * 7));
        }
    };
    Easing EASE = t -> {
        // default css timing function "ease"
        // Polynomial approximation of cubic-bezier(0.25, 0.1, 0.25, 1.0)
        if (t < 0) return 0;
        if (t > 1) return 1;

        // Approximation using a 5th degree polynomial
        return t * t * t * (t * (t * 6 - 15) + 10);
    };

    static Easing newCubicBezier(float x1, float y1, float x2, float y2) {
        return new CubicBezier(x1, y1, x2, y2);
    }

    // https://github.com/WebKit/WebKit/blob/7db30881c742241a3861d9ff9aca6486397d4d2c/Source/WebCore/platform/graphics/UnitBezier.h
    class CubicBezier implements Easing {
        private static final int CUBIC_BEZIER_SPLINE_SAMPLES = 11;
        private static final double kBezierEpsilon = 1e-7;
        private static final int kMaxNewtonIterations = 4;

        private final double ax, bx, cx;
        private final double ay, by, cy;
        private final double startGradient, endGradient;
        private final double[] splineSamples = new double[CUBIC_BEZIER_SPLINE_SAMPLES];

        CubicBezier(float p1x, float p1y, float p2x, float p2y) {
            // Calculate the polynomial coefficients, implicit first and last control points are (0,0) and (1,1).
            cx = 3.0 * p1x;
            bx = 3.0 * (p2x - p1x) - cx;
            ax = 1.0 - cx - bx;

            cy = 3.0 * p1y;
            by = 3.0 * (p2y - p1y) - cy;
            ay = 1.0 - cy - by;

            // End-point gradients are used to calculate timing function results
            // outside the range [0, 1].
            //
            // There are four possibilities for the gradient at each end:
            // (1) the closest control point is not horizontally coincident with regard to
            //     (0, 0) or (1, 1). In this case the line between the end point and
            //     the control point is tangent to the bezier at the end point.
            // (2) the closest control point is coincident with the end point. In
            //     this case the line between the end point and the far control
            //     point is tangent to the bezier at the end point.
            // (3) both internal control points are coincident with an endpoint. There
            //     are two special case that fall into this category:
            //     CubicBezier(0, 0, 0, 0) and CubicBezier(1, 1, 1, 1). Both are
            //     equivalent to linear.
            // (4) the closest control point is horizontally coincident with the end
            //     point, but vertically distinct. In this case the gradient at the
            //     end point is Infinite. However, this causes issues when
            //     interpolating. As a result, we break down to a simple case of
            //     0 gradient under these conditions.
            if (p1x > 0)
                startGradient = p1y / p1x;
            else if (p1y == 0 && p2x > 0)
                startGradient = p2y / p2x;
            else if (p1y == 0 && p2y == 0)
                startGradient = 1;
            else
                startGradient = 0;
            if (p2x < 1)
                endGradient = (p2y - 1) / (p2x - 1);
            else if (p2y == 1 && p1x < 1)
                endGradient = (p1y - 1) / (p1x - 1);
            else if (p2y == 1 && p1y == 1)
                endGradient = 1;
            else
                endGradient = 0;

            double deltaT = 1.0 / (CUBIC_BEZIER_SPLINE_SAMPLES - 1);
            for (int i = 0; i < CUBIC_BEZIER_SPLINE_SAMPLES; i++)
                splineSamples[i] = sampleCurveX(i * deltaT);
        }

        private double sampleCurveX(double t) {
            // `ax t^3 + bx t^2 + cx t' expanded using Horner's rule.
            return ((ax * t + bx) * t + cx) * t;
        }

        private double sampleCurveY(double t) {
            return ((ay * t + by) * t + cy) * t;
        }

        private double sampleCurveDerivativeX(double t) {
            return (3.0 * ax * t + 2.0 * bx) * t + cx;
        }

        // Given an x value, find a parametric value it came from.
        private double solveCurveX(double x, double epsilon) {
            double t0 = 0.0;
            double t1 = 0.0;
            double t2 = x;
            double x2 = 0.0;
            double d2 = 0.0;
            int i = 0;

            // Linear interpolation of spline curve for initial guess.
            double deltaT = 1.0 / (CUBIC_BEZIER_SPLINE_SAMPLES - 1);
            for (i = 1; i < CUBIC_BEZIER_SPLINE_SAMPLES; i++) {
                if (x <= splineSamples[i]) {
                    t1 = deltaT * i;
                    t0 = t1 - deltaT;
                    t2 = t0 + (t1 - t0) * (x - splineSamples[i - 1]) / (splineSamples[i] - splineSamples[i - 1]);
                    break;
                }
            }

            // Perform a few iterations of Newton's method -- normally very fast.
            // See https://en.wikipedia.org/wiki/Newton%27s_method.
            double newtonEpsilon = Math.min(kBezierEpsilon, epsilon);
            for (i = 0; i < kMaxNewtonIterations; i++) {
                x2 = sampleCurveX(t2) - x;
                if (Math.abs(x2) < newtonEpsilon)
                    return t2;
                d2 = sampleCurveDerivativeX(t2);
                if (Math.abs(d2) < kBezierEpsilon)
                    break;
                t2 = t2 - x2 / d2;
            }
            if (Math.abs(x2) < epsilon)
                return t2;

            // Fall back to the bisection method for reliability.
            while (t0 < t1) {
                x2 = sampleCurveX(t2);
                if (Math.abs(x2 - x) < epsilon)
                    return t2;
                if (x > x2)
                    t0 = t2;
                else
                    t1 = t2;
                t2 = (t1 + t0) * .5;
            }

            // Failure.
            return t2;
        }

        private double solve(double x, double epsilon) {
            if (x < 0.0)
                return 0.0 + startGradient * x;
            if (x > 1.0)
                return 1.0 + endGradient * (x - 1.0);
            return sampleCurveY(solveCurveX(x, epsilon));
        }

        @Override
        public float ease(float t) {
            return (float) solve(t, 1e-6);
        }
    }
}
