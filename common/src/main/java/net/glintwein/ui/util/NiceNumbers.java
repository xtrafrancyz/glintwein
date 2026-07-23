package net.glintwein.ui.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class NiceNumbers {

    /**
     * Find a "nice" number approximately equal to x.
     * Round the number if round == true, take ceiling if round == false.
     */
    public static double niceNum(double x, boolean round) {
        if (x == 0) return 0;

        int exp = (int) Math.floor(Math.log10(x));   // exponent of x
        double f = x / Math.pow(10, exp);             // fractional part, 1 <= f < 10
        double nf; // nice, rounded fraction

        if (round) {
            if (f < 1.5) nf = 1;
            else if (f < 3) nf = 2;
            else if (f < 7) nf = 5;
            else nf = 10;
        } else {
            if (f <= 1) nf = 1;
            else if (f <= 2) nf = 2;
            else if (f <= 5) nf = 5;
            else nf = 10;
        }

        return nf * Math.pow(10, exp);
    }

    /**
     * Compute a set of nicely rounded tick values spanning [min, max].
     *
     * @param min      lower end of data range
     * @param max      upper end of data range
     * @param maxTicks desired (approximate) number of tick intervals
     * @return array of tick values, from a "nice" min to a "nice" max
     */
    public static double[] niceTicks(double min, double max, int maxTicks) {
        double range = niceNum(max - min, false);
        double tickSpacing = niceNum(range / (maxTicks - 1), true);
        double niceMin = Math.floor(min / tickSpacing) * tickSpacing;
        double niceMax = Math.ceil(max / tickSpacing) * tickSpacing;

        int count = (int) Math.round((niceMax - niceMin) / tickSpacing) + 1;
        double[] ticks = new double[count];
        for (int i = 0; i < count; i++) {
            ticks[i] = niceMin + i * tickSpacing;
        }
        return ticks;
    }

    // Demo
    public static void main(String[] args) {
        double min = 3.7;
        double max = 92.4;
        int maxTicks = 8;

        double[] ticks = niceTicks(min, max, maxTicks);

        System.out.printf("Range: [%.2f, %.2f], target ticks: %d%n", min, max, maxTicks);
        System.out.print("Nice ticks: ");
        for (double t : ticks) {
            System.out.printf("%.2f ", t);
        }
        System.out.println();
    }

    /**
     * Formats "nice" tick values produced by Heckbert's Nice Numbers algorithm.
     * Automatically determines how many decimal places are needed based on the
     * tick spacing, and cleans up floating-point noise (e.g. 0.30000000000000004).
     */
    public static class Formatter {

        /**
         * Build a formatter appropriate for a given tick spacing.
         * e.g. spacing = 0.5  -> "0.0" pattern
         * spacing = 5    -> "0" pattern
         * spacing = 0.05 -> "0.00" pattern
         */
        public static DecimalFormat forSpacing(double tickSpacing) {
            int decimals = decimalPlacesNeeded(tickSpacing);
            StringBuilder pattern = new StringBuilder("0");
            if (decimals > 0) {
                pattern.append(".");
                for (int i = 0; i < decimals; i++) pattern.append("0");
            }
            DecimalFormat df = new DecimalFormat(pattern.toString(),
                DecimalFormatSymbols.getInstance(Locale.US));
            return df;
        }

        /**
         * Determine how many decimal places are needed to distinguish
         * ticks at the given spacing, based on its power-of-ten exponent.
         */
        public static int decimalPlacesNeeded(double tickSpacing) {
            if (tickSpacing == 0) return 0;
            int exp = (int) Math.floor(Math.log10(Math.abs(tickSpacing)));
            // Guard against floating point edge cases right at a power of ten
            double rounded = Math.round(tickSpacing / Math.pow(10, exp)) * Math.pow(10, exp);
            exp = (int) Math.floor(Math.log10(Math.abs(rounded)) + 1e-9);
            return Math.max(0, -exp);
        }

        /**
         * Format a single tick value using nice spacing-aware precision.
         */
        public static String format(double value, double tickSpacing) {
            return format(forSpacing(tickSpacing), value, tickSpacing);
        }

        public static String format(DecimalFormat df, double value, double tickSpacing) {
            return df.format(cleanFloatingPoint(value, tickSpacing));
        }

        /**
         * Format an entire array of ticks, all with consistent decimal places.
         */
        public static String[] formatAll(double[] ticks, double tickSpacing) {
            DecimalFormat df = forSpacing(tickSpacing);
            String[] out = new String[ticks.length];
            for (int i = 0; i < ticks.length; i++) {
                out[i] = df.format(cleanFloatingPoint(ticks[i], tickSpacing));
            }
            return out;
        }

        /**
         * Snap a value to the nearest multiple of tickSpacing to eliminate
         * floating-point drift (e.g. 0.30000000000000004 -> 0.3).
         */
        private static double cleanFloatingPoint(double value, double tickSpacing) {
            if (tickSpacing == 0) return value;
            double snapped = Math.round(value / tickSpacing) * tickSpacing;
            // Only trust the snap if it's very close to the original value
            return Math.abs(snapped - value) < tickSpacing * 1e-6 ? snapped : value;
        }

        // Demo
        public static void main(String[] args) {
            double min = 3.7;
            double max = 92.4;
            int maxTicks = 8;

            double range = niceNum(max - min, false);
            double tickSpacing = niceNum(range / (maxTicks - 1), true);
            double[] ticks = niceTicks(min, max, maxTicks);

            System.out.println("Tick spacing: " + tickSpacing);
            for (String s : formatAll(ticks, tickSpacing)) {
                System.out.println(s);
            }

            // Edge case demo: fractional spacing
            double[] fineTicks = niceTicks(0.12, 0.94, 6);
            double fineRange = niceNum(0.94 - 0.12, false);
            double fineSpacing = niceNum(fineRange / 5, true);
            System.out.println("\nFine tick spacing: " + fineSpacing);
            for (String s : formatAll(fineTicks, fineSpacing)) {
                System.out.println(s);
            }
        }
    }
}
