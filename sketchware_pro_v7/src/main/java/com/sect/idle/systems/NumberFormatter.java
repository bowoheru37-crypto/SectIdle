package com.sect.idle.systems;

/**
 * NumberFormatter - High-performance number formatting utility.
 * Optimized for 60 FPS rendering on memory-constrained devices (e.g. Itel A70 1GB RAM).
 * Avoids java.text.DecimalFormat overhead and eliminates intermediate string object churn
 * when paired with reusable StringBuilders.
 */
public final class NumberFormatter {
    private static final String[] SUFFIXES = {"","K","M","B","T","Qa","Qi","Sx","Sp","Oc","No","Dc"};

    private NumberFormatter() {}

    /**
     * Appends a long integer formatted with standard thousands separators (e.g. 123,456)
     * directly into a StringBuilder without DecimalFormat overhead or extra heap allocations.
     */
    public static void appendFormattedInt(long num, StringBuilder sb) {
        if (num < 0) {
            sb.append('-');
            num = -num;
        }
        if (num < 1000L) {
            sb.append(num);
            return;
        }
        long thousands = num / 1000L;
        int rem = (int)(num % 1000L);
        appendFormattedInt(thousands, sb);
        sb.append(',');
        if (rem < 10) sb.append("00");
        else if (rem < 100) sb.append('0');
        sb.append(rem);
    }

    /**
     * Appends a double formatted to 1 decimal place (e.g. 12.3) directly into a StringBuilder.
     */
    public static void append1Dec(double num, StringBuilder sb) {
        if (num < 0) {
            sb.append('-');
            num = -num;
        }
        long scaled = (long)(num * 10.0 + 0.5);
        long integral = scaled / 10;
        int dec = (int)(scaled % 10);
        sb.append(integral).append('.').append(dec);
    }

    /**
     * Appends a double formatted to 2 decimal places (e.g. 12.34) directly into a StringBuilder.
     */
    public static void append2Dec(double num, StringBuilder sb) {
        if (num < 0) {
            sb.append('-');
            num = -num;
        }
        long scaled = (long)(num * 100.0 + 0.5);
        long integral = scaled / 100;
        int dec = (int)(scaled % 100);
        sb.append(integral).append('.');
        if (dec < 10) sb.append('0');
        sb.append(dec);
    }

    public static String format(long num) {
        StringBuilder sb = new StringBuilder(16);
        format(num, sb);
        return sb.toString();
    }

    public static String format(long num, StringBuilder sb) {
        if (sb == null) sb = new StringBuilder(16);
        else sb.setLength(0);

        if (num < 1000L) {
            sb.append(num);
            return sb.toString();
        }
        if (num < 1000000L) {
            appendFormattedInt(num, sb);
            return sb.toString();
        }

        int idx = 0;
        double d = num;
        while (d >= 1000.0 && idx < SUFFIXES.length - 1) {
            d /= 1000.0;
            idx++;
        }
        append1Dec(d, sb);
        sb.append(SUFFIXES[idx]);
        return sb.toString();
    }

    public static String format(double num) {
        StringBuilder sb = new StringBuilder(16);
        format(num, sb);
        return sb.toString();
    }

    public static String format(double num, StringBuilder sb) {
        if (sb == null) sb = new StringBuilder(16);
        else sb.setLength(0);

        if (num < 1000.0) {
            append2Dec(num, sb);
            return sb.toString();
        }

        int idx = 0;
        double d = num;
        while (d >= 1000.0 && idx < SUFFIXES.length - 1) {
            d /= 1000.0;
            idx++;
        }
        append1Dec(d, sb);
        sb.append(SUFFIXES[idx]);
        return sb.toString();
    }

    public static String formatInt(long num) {
        StringBuilder sb = new StringBuilder(16);
        appendFormattedInt(num, sb);
        return sb.toString();
    }

    public static String formatPercent(float f) {
        return (int)(f * 100f) + "%";
    }

    public static String formatPercent(float f, int decimals) {
        if (decimals <= 0) return (int)(f * 100f) + "%";
        float pow = 1f;
        for (int i = 0; i < decimals; i++) pow *= 10f;
        return ((int)(f * 100f * pow) / pow) + "%";
    }

    public static String formatTime(int seconds) {
        if (seconds < 60) return seconds + "s";
        if (seconds < 3600) return (seconds / 60) + "m " + (seconds % 60) + "s";
        if (seconds < 86400) return (seconds / 3600) + "h " + ((seconds % 3600) / 60) + "m";
        return (seconds / 86400) + "d " + ((seconds % 86400) / 3600) + "h";
    }

    public static String formatRoman(int num) {
        if (num <= 0) return "";
        StringBuilder sb = new StringBuilder(16);
        int[] values = {1000,900,500,400,100,90,50,40,10,9,5,4,1};
        String[] symbols = {"M","CM","D","CD","C","XC","L","XL","X","IX","V","IV","I"};
        for (int i = 0; i < values.length; i++) {
            while (num >= values[i]) { sb.append(symbols[i]); num -= values[i]; }
        }
        return sb.toString();
    }

    public static String formatCompact(long num, StringBuilder sb) {
        return format(num, sb);
    }
}
