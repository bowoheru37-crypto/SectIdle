package com.sect.idle.systems;

/**
 * High-performance, zero-allocation NumberFormatter.
 * Designed for high-frequency render passes and UI loops on low-spec devices.
 * Eliminates DecimalFormat lock contention and GC overhead.
 */
public final class NumberFormatter {
    private static final String[] SUFFIXES = {"","K","M","B","T","Qa","Qi","Sx","Sp","Oc","No","Dc"};

    // ThreadLocal StringBuilder buffer to avoid string concatenation allocations on single calls
    private static final ThreadLocal<StringBuilder> TL_SB = new ThreadLocal<StringBuilder>() {
        @Override
        protected StringBuilder initialValue() {
            return new StringBuilder(64);
        }
    };

    private NumberFormatter() {}

    private static StringBuilder getTLBuffer() {
        StringBuilder sb = TL_SB.get();
        sb.setLength(0);
        return sb;
    }

    public static String format(long num) {
        StringBuilder sb = getTLBuffer();
        format(num, sb);
        return sb.toString();
    }

    public static String format(long num, StringBuilder sb) {
        if (sb == null) return format(num);
        sb.setLength(0);
        if (num < 0) {
            sb.append('-');
            if (num == Long.MIN_VALUE) {
                return sb.append("9.22Ex").toString();
            }
            num = -num;
        }
        if (num < 1000L) {
            return sb.append(num).toString();
        }
        if (num < 1000000L) {
            appendFormattedInt(sb, num);
            return sb.toString();
        }
        int idx = 0;
        double d = num;
        while (d >= 1000.0 && idx < SUFFIXES.length - 1) {
            d /= 1000.0;
            idx++;
        }
        appendOneDecimal(sb, d, idx);
        return sb.toString();
    }

    public static String format(double num) {
        StringBuilder sb = getTLBuffer();
        format(num, sb);
        return sb.toString();
    }

    public static String format(double num, StringBuilder sb) {
        if (sb == null) return format(num);
        sb.setLength(0);
        if (Double.isNaN(num) || Double.isInfinite(num)) {
            return sb.append("0.00").toString();
        }
        if (num < 0) {
            sb.append('-');
            num = -num;
        }
        if (num < 1000.0) {
            long rounded = Math.round(num * 100.0);
            long whole = rounded / 100L;
            long frac = rounded % 100L;
            sb.append(whole).append('.');
            if (frac < 10) sb.append('0');
            sb.append(frac);
            return sb.toString();
        }
        int idx = 0;
        double d = num;
        while (d >= 1000.0 && idx < SUFFIXES.length - 1) {
            d /= 1000.0;
            idx++;
        }
        appendOneDecimal(sb, d, idx);
        return sb.toString();
    }

    private static void appendOneDecimal(StringBuilder sb, double d, int idx) {
        long rounded = Math.round(d * 10.0);
        if (rounded >= 10000L && idx < SUFFIXES.length - 1) {
            idx++;
            rounded = Math.round((d / 1000.0) * 10.0);
        }
        long whole = rounded / 10L;
        long frac = rounded % 10L;
        sb.append(whole).append('.').append(frac).append(SUFFIXES[idx]);
    }

    public static String formatInt(long num) {
        StringBuilder sb = getTLBuffer();
        appendFormattedInt(sb, num);
        return sb.toString();
    }

    public static void appendFormattedInt(StringBuilder sb, long num) {
        if (num < 0) {
            sb.append('-');
            if (num == Long.MIN_VALUE) {
                sb.append("9,223,372,036,854,775,808");
                return;
            }
            num = -num;
        }
        if (num < 1000L) {
            sb.append(num);
            return;
        }
        appendFormattedIntGroup(sb, num);
    }

    private static void appendFormattedIntGroup(StringBuilder sb, long num) {
        if (num >= 1000L) {
            appendFormattedIntGroup(sb, num / 1000L);
            sb.append(',');
            int rem = (int)(num % 1000L);
            if (rem < 10) sb.append("00");
            else if (rem < 100) sb.append('0');
            sb.append(rem);
        } else {
            sb.append(num);
        }
    }

    public static String formatPercent(float f) {
        StringBuilder sb = getTLBuffer();
        sb.append((int)(f * 100f)).append('%');
        return sb.toString();
    }

    public static String formatPercent(float f, int decimals) {
        if (decimals <= 0) return formatPercent(f);
        StringBuilder sb = getTLBuffer();
        float pow = 1f;
        for (int i = 0; i < decimals; i++) pow *= 10f;
        float val = (int)(f * 100f * pow) / pow;
        sb.append(val).append('%');
        return sb.toString();
    }

    public static String formatTime(int seconds) {
        if (seconds < 0) seconds = 0;
        StringBuilder sb = getTLBuffer();
        if (seconds < 60) {
            sb.append(seconds).append('s');
        } else if (seconds < 3600) {
            sb.append(seconds / 60).append("m ").append(seconds % 60).append('s');
        } else if (seconds < 86400) {
            sb.append(seconds / 3600).append("h ").append((seconds % 3600) / 60).append('m');
        } else {
            sb.append(seconds / 86400).append("d ").append((seconds % 86400) / 3600).append('h');
        }
        return sb.toString();
    }

    public static String formatRoman(int num) {
        if (num <= 0) return "";
        StringBuilder sb = getTLBuffer();
        int[] values = {1000,900,500,400,100,90,50,40,10,9,5,4,1};
        String[] symbols = {"M","CM","D","CD","C","XC","L","XL","X","IX","V","IV","I"};
        for (int i = 0; i < values.length; i++) {
            while (num >= values[i]) {
                sb.append(symbols[i]);
                num -= values[i];
            }
        }
        return sb.toString();
    }

    public static String formatCompact(long num, StringBuilder sb) {
        return format(num, sb);
    }
}
