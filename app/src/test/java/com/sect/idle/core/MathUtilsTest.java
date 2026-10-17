package com.sect.idle.core;

import org.junit.Assert;
import org.junit.Test;

public class MathUtilsTest {

    private static final float DELTA = 0.02f;

    @Test
    public void testSinAndCosValues() {
        // Test key angles (0, PI/2, PI, 3PI/2, 2PI)
        Assert.assertEquals(0f, MathUtils.sin(0f), DELTA);
        Assert.assertEquals(1f, MathUtils.cos(0f), DELTA);

        Assert.assertEquals(1f, MathUtils.sin((float) Math.PI / 2f), DELTA);
        Assert.assertEquals(0f, MathUtils.cos((float) Math.PI / 2f), DELTA);

        Assert.assertEquals(0f, MathUtils.sin((float) Math.PI), DELTA);
        Assert.assertEquals(-1f, MathUtils.cos((float) Math.PI), DELTA);

        Assert.assertEquals(-1f, MathUtils.sin((float) (3 * Math.PI / 2f)), DELTA);
        Assert.assertEquals(0f, MathUtils.cos((float) (3 * Math.PI / 2f)), DELTA);

        Assert.assertEquals(0f, MathUtils.sin((float) (2 * Math.PI)), DELTA);
        Assert.assertEquals(1f, MathUtils.cos((float) (2 * Math.PI)), DELTA);
    }

    @Test
    public void testNegativeAndWrappedAngles() {
        // Negative angles
        Assert.assertEquals(-1f, MathUtils.sin((float) (-Math.PI / 2f)), DELTA);
        Assert.assertEquals(0f, MathUtils.cos((float) (-Math.PI / 2f)), DELTA);

        Assert.assertEquals(0f, MathUtils.sin((float) (-Math.PI)), DELTA);
        Assert.assertEquals(-1f, MathUtils.cos((float) (-Math.PI)), DELTA);

        // Wrapped positive angles (> 2PI)
        Assert.assertEquals(1f, MathUtils.sin((float) (2.5 * Math.PI)), DELTA);
        Assert.assertEquals(0f, MathUtils.cos((float) (2.5 * Math.PI)), DELTA);

        // Wrapped negative angles (< -2PI)
        Assert.assertEquals(-1f, MathUtils.sin((float) (-2.5 * Math.PI)), DELTA);
        Assert.assertEquals(0f, MathUtils.cos((float) (-2.5 * Math.PI)), DELTA);
    }
}
