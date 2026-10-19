package com.sect.idle.systems;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class NumberFormatterTest {

    @Test
    public void testFormatLong() {
        assertEquals("0", NumberFormatter.format(0L));
        assertEquals("999", NumberFormatter.format(999L));
        assertEquals("1,000", NumberFormatter.format(1000L));
        assertEquals("12,345", NumberFormatter.format(12345L));
        assertEquals("999,999", NumberFormatter.format(999999L));
        assertEquals("1.0M", NumberFormatter.format(1000000L));
        assertEquals("1.2M", NumberFormatter.format(1234567L));
        assertEquals("1.0B", NumberFormatter.format(1000000000L));
        assertEquals("-1,234", NumberFormatter.format(-1234L));
        assertEquals("-1.2M", NumberFormatter.format(-1234567L));
    }

    @Test
    public void testFormatLongWithStringBuilder() {
        StringBuilder sb = new StringBuilder();
        assertEquals("0", NumberFormatter.format(0L, sb));
        assertEquals("999", NumberFormatter.format(999L, sb));
        assertEquals("1,000", NumberFormatter.format(1000L, sb));
        assertEquals("12,345", NumberFormatter.format(12345L, sb));
        assertEquals("1.2M", NumberFormatter.format(1234567L, sb));
        assertEquals("-12,345", NumberFormatter.format(-12345L, sb));
    }

    @Test
    public void testFormatDouble() {
        assertEquals("0.00", NumberFormatter.format(0.0));
        assertEquals("12.34", NumberFormatter.format(12.34));
        assertEquals("12.00", NumberFormatter.format(12.0));
        assertEquals("999.99", NumberFormatter.format(999.99));
        assertEquals("1.0K", NumberFormatter.format(1000.0));
        assertEquals("1.2M", NumberFormatter.format(1234567.89));
        assertEquals("-0.40", NumberFormatter.format(-0.4));
        assertEquals("-12.34", NumberFormatter.format(-12.34));
    }

    @Test
    public void testFormatDoubleWithStringBuilder() {
        StringBuilder sb = new StringBuilder();
        assertEquals("0.00", NumberFormatter.format(0.0, sb));
        assertEquals("12.34", NumberFormatter.format(12.34, sb));
        assertEquals("1.2M", NumberFormatter.format(1234567.89, sb));
        assertEquals("-0.50", NumberFormatter.format(-0.5, sb));
    }

    @Test
    public void testFormatTime() {
        assertEquals("45s", NumberFormatter.formatTime(45));
        assertEquals("2m 30s", NumberFormatter.formatTime(150));
        assertEquals("1h 0m", NumberFormatter.formatTime(3600));
        assertEquals("1d 2h", NumberFormatter.formatTime(93600));
    }

    @Test
    public void testFormatRoman() {
        assertEquals("I", NumberFormatter.formatRoman(1));
        assertEquals("IV", NumberFormatter.formatRoman(4));
        assertEquals("IX", NumberFormatter.formatRoman(9));
        assertEquals("X", NumberFormatter.formatRoman(10));
        assertEquals("XLII", NumberFormatter.formatRoman(42));
    }
}
