/**
 * 
 */
package jflowmap.util;

import junit.framework.TestCase;

/**
 * @author ilya
 *
 */
public class LogScaleTest extends TestCase {

	static final double EPS = 1e-7; 
	/**
	 * Test method for {@link jflowmap.util.LogScale#linearToLog(double)}.
	 */
	public void testLinearToLog() {
		
		LogScale ls = new LogScale.Builder(-100, 100).build();
//		ls.linearToLog(x);
	}
	
	public void testLog() {
		assertEquals(2, LogScale.log(4, 2), EPS);
		assertEquals(6, LogScale.log(64, 2), EPS);
		assertEquals(Math.log(2), LogScale.log(2, Math.E), EPS);
		assertEquals(Math.log(16), LogScale.log(16, Math.E), EPS);
		assertEquals(3, LogScale.log(1000, 10), EPS);
	}	

}
