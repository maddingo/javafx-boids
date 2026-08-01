package com.tofti;

import org.junit.Assert;
import org.junit.Test;

public class DoubleUtilTest {
    private static double TOLERANCE = 1E-6;
    @Test
    public void testMaxSignInsensitive() {
        Assert.assertEquals(5d, DoubleUtil.signSensitiveMax(5.0d, 10.d), TOLERANCE);
        Assert.assertEquals(10.0d, DoubleUtil.signSensitiveMax(10.0d, 10.d), TOLERANCE);
        Assert.assertEquals(10.0d, DoubleUtil.signSensitiveMax(11.0d, 10.d), TOLERANCE);

        Assert.assertEquals(-5d, DoubleUtil.signSensitiveMax(-5.0d, -10.d), TOLERANCE);
        Assert.assertEquals(-10.0d, DoubleUtil.signSensitiveMax(-10.0d, -10.d), TOLERANCE);
        Assert.assertEquals(-10.0d, DoubleUtil.signSensitiveMax(-11.0d, -10.d), TOLERANCE);
    }

}