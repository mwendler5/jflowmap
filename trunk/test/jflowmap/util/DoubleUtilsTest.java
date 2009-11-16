package jflowmap.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class DoubleUtilsTest {
	private final static double EPS = 1e-7;

	@Test
	public void testMagnitude() {
		assertEquals(100, DoubleUtils.magnitude(234.543), EPS);
	}

}
