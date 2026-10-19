package com.sect.idle.systems;

/**
 * NumberFormatter - High performance string formatting utility for numeric values.
 * Optimized to eliminate DecimalFormat synchronization lock contention and GC object allocations.
 */
public final class NumberFormatter {
    private static final String[] SUFFIXES = {"","K","M","B","T","Qa","Qi","Sx","Sp","Oc","No","Dc"};

    private NumberFormatter() {}

    /**
     * Formats a double with 1 decimal place (0.0) without DecimalFormat overhead.
     */
    private static String formatOneDecimal(double val) {
        if (Double.isNaN(val) || Double.isInfinite(val)) return String.valueOf(val);
        boolean negative = val < 0;
        double absVal = Math.abs(val);
        long scaled = Math.round(absVal * 10.0);
        long integral = scaled / 10;
        long fractional = scaled % 10;
        return (negative ? "-" : "") + integral + "." + fractional;
    }

    /**
     * Appends a double formatted with 1 decimal place (0.0) directly into a StringBuilder.
     */
    private static void appendOneDecimal(StringBuilder sb, double val) {
        if (Double.isNaN(val) || Double.isInfinite(val)) {
            sb.append(val);
            return;
        }
        if (val < 0) sb.append('-');
        double absVal = Math.abs(val);
        long scaled = Math.round(absVal * 10.0);
        long integral = scaled / 10;
        long fractional = scaled % 10;
        sb.append(integral).append('.').append(fractional);
    }

    /**
     * Formats a double with 2 decimal places (0.00) without DecimalFormat overhead.
     */
    private static String formatTwoDecimals(double val) {
        if (Double.isNaN(val) || Double.isInfinite(val)) return String.valueOf(val);
        boolean negative = val < 0;
        double absVal = Math.abs(val);
        long scaled = Math.round(absVal * 100.0);
        long integral = scaled / 100;
        long fractional = scaled % 100;
        if (fractional < 10) {
            return (negative ? "-" : "") + integral + ".0" + fractional;
        } else {
            return (negative ? "-" : "") + integral + "." + fractional;
        }
    }

    /**
     * Appends a double formatted with 2 decimal places (0.00) directly into a StringBuilder.
     */
    private static void appendTwoDecimals(StringBuilder sb, double val) {
        if (Double.isNaN(val) || Double.isInfinite(val)) {
            sb.append(val);
            return;
        }
        if (val < 0) sb.append('-');
        double absVal = Math.abs(val);
        long scaled = Math.round(absVal * 100.0);
        long integral = scaled / 100;
        long fractional = scaled % 100;
        sb.append(integral).append('.');
        if (fractional < 10) sb.append('0');
        sb.append(fractional);
    }

    /**
     * Formats a long with thousands separators without DecimalFormat overhead.
     */
    private static String formatWithCommas(long num) {
        if (num == 0) return "0";
        boolean negative = num < 0;
        long absNum = Math.abs(num);
        String s = String.valueOf(absNum);
        int len = s.length();
        StringBuilder sb = new StringBuilder(len + len / 3 + (negative ? 1 : 0));
        if (negative) sb.append('-');
        int firstGroup = len % 3;
        if (firstGroup == 0) firstGroup = 3;
        sb.append(s, 0, firstGroup);
        for (int i = firstGroup; i < len; i += 3) {
            sb.append(',').append(s, i, i + 3);
        }
        return sb.toString();
    }

    /**
     * Appends a long with thousands separators directly into a StringBuilder.
     */
    private static void appendWithCommas(StringBuilder sb, long num) {
        if (num == 0) {
            sb.append('0');
            return;
        }
        boolean negative = num < 0;
        long absNum = Math.abs(num);
        String s = String.valueOf(absNum);
        int len = s.length();
        if (negative) sb.append('-');
        int firstGroup = len % 3;
        if (firstGroup == 0) firstGroup = 3;
        sb.append(s, 0, firstGroup);
        for (int i = firstGroup; i < len; i += 3) {
            sb.append(',').append(s, i, i + 3);
        }
    }

    public static String format(long num) {
        if (num > -1000L && num < 1000L) return String.valueOf(num);
        if (num > -1000000L && num < 1000000L) return formatWithCommas(num);
        int idx = 0;
        double d = num;
        while (Math.abs(d) >= 1000.0 && idx < SUFFIXES.length - 1) { d /= 1000.0; idx++; }
        return formatOneDecimal(d) + SUFFIXES[idx];
    }

    public static String format(long num, StringBuilder sb) {
        if (sb == null) return format(num);
        sb.setLength(0);
        if (num > -1000L && num < 1000L) {
            sb.append(num);
            return sb.toString();
        }
        if (num > -1000000L && num < 1000000L) {
            appendWithCommas(sb, num);
            return sb.toString();
        }
        int idx = 0;
        double d = num;
        while (Math.abs(d) >= 1000.0 && idx < SUFFIXES.length - 1) { d /= 1000.0; idx++; }
        appendOneDecimal(sb, d);
        sb.append(SUFFIXES[idx]);
        return sb.toString();
    }

    public static String format(double num) {
        if (Math.abs(num) < 1000.0) return formatTwoDecimals(num);
        int idx = 0;
        double d = num;
        while (Math.abs(d) >= 1000.0 && idx < SUFFIXES.length - 1) { d /= 1000.0; idx++; }
        return formatOneDecimal(d) + SUFFIXES[idx];
    }

    public static String format(double num, StringBuilder sb) {
        if (sb == null) return format(num);
        sb.setLength(0);
        if (Math.abs(num) < 1000.0) {
            appendTwoDecimals(sb, num);
            return sb.toString();
        }
        int idx = 0;
        double d = num;
        while (Math.abs(d) >= 1000.0 && idx < SUFFIXES.length - 1) { d /= 1000.0; idx++; }
        appendOneDecimal(sb, d);
        sb.append(SUFFIXES[idx]);
        return sb.toString();
    }

    public static String formatInt(long num) {
        return formatWithCommas(num);
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
