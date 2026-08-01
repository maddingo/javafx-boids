package com.tofti;

import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class Vector2DTest extends TestCase {

    private static final double TOLERANCE = 1E-6;
    public static final double SQRT_TWO = Math.sqrt(2.0);

    @Test
    public void testGetArithmeticMean() {
        Vector2D x1  = Vector2D.getArithmeticMean(Arrays.asList(new Vector2D(2d, 2d), new Vector2D(4d, 4d)));
        Assert.assertEquals(3d, x1.getX(), TOLERANCE);
        Assert.assertEquals(3d, x1.getY(), TOLERANCE);

        Vector2D x2  = Vector2D.getArithmeticMean(Arrays.asList(new Vector2D(-2d, -2d), new Vector2D(2d, 2d)));
        Assert.assertEquals(0d, x2.getX(), TOLERANCE);
        Assert.assertEquals(0d, x2.getY(), TOLERANCE);
    }


    @Test
    public void testNormalize() {
        Assert.assertEquals(1.0, new Vector2D(10d, 0d).normalize().getX(), TOLERANCE);
        Assert.assertEquals(0.0, new Vector2D(10d, 0d).normalize().getY(), TOLERANCE);
        Assert.assertEquals(0.0, new Vector2D(0d, 10d).normalize().getX(), TOLERANCE);
        Assert.assertEquals(1.0, new Vector2D(0d, 10d).normalize().getY(), TOLERANCE);

        Assert.assertEquals(0.6, new Vector2D(3d, 4d).normalize().getX(), TOLERANCE);
        Assert.assertEquals(0.8, new Vector2D(3d, 4d).normalize().getY(), TOLERANCE);

        Assert.assertEquals(-0.6, new Vector2D(-3d, -4d).normalize().getX(), TOLERANCE);
        Assert.assertEquals(-0.8, new Vector2D(-3d, -4d).normalize().getY(), TOLERANCE);

        Assert.assertEquals(1d, new Vector2D(3d, 4d).normalize().getMagnitude(), TOLERANCE);
        Assert.assertEquals(1d, new Vector2D(-3d, -4d).normalize().getMagnitude(), TOLERANCE);

        Assert.assertEquals(0d, new Vector2D(0d, 0d).normalize().getX(), TOLERANCE);
        Assert.assertEquals(0d, new Vector2D(0d, 0d).normalize().getY(), TOLERANCE);

        Assert.assertEquals(1.2, new Vector2D(3d, 4d).normalizeTo(2d).getX(), TOLERANCE);
        Assert.assertEquals(1.6, new Vector2D(3d, 4d).normalizeTo(2d).getY(), TOLERANCE);
    }

    @Test
    public void testGetMagnitude() {
        Assert.assertEquals(2.82842712, new Vector2D(2d, 2d).getMagnitude(), TOLERANCE);
        Assert.assertEquals(2.82842712, new Vector2D(-2d, 2d).getMagnitude(), TOLERANCE);
        Assert.assertEquals(2.82842712, new Vector2D(-2d, -2d).getMagnitude(), TOLERANCE);
        Assert.assertEquals(2.82842712, new Vector2D(2d, -2d).getMagnitude(), TOLERANCE);
        Assert.assertEquals(3.60555127546, new Vector2D(3d, -2d).getMagnitude(), TOLERANCE);
        Assert.assertEquals(3.60555127546, new Vector2D(-3d, -2d).getMagnitude(), TOLERANCE);
        Assert.assertEquals(3.60555127546, new Vector2D(-3d, -2d).getMagnitude(), TOLERANCE);
        Assert.assertEquals(3.60555127546, new Vector2D(3d, -2d).getMagnitude(), TOLERANCE);
    }

    @Test
    public void testDistanceBetween() {
        Assert.assertEquals(SQRT_TWO, new Vector2D(2d, 2d).getDistanceFrom(new Vector2D(3d,3d)), 0.01);
        Assert.assertEquals(SQRT_TWO, new Vector2D(-2d, -2d).getDistanceFrom(new Vector2D(-3d,-3d)), 0.01);
        Assert.assertEquals(9.219544457292887, new Vector2D(-3d, -4d).getDistanceFrom(new Vector2D(3d,3d)), 0.01);
    }
}