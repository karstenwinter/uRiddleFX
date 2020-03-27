package uriddle.logic.test;

import junit.framework.*;
import uriddle.logic.*;

public class GeneratorTest extends TestCase {
	public void testGenerator() throws Exception {
		StringBuilder stringBuilder = new StringBuilder("\n");
		for (int i = 0; i < 128; i++) {
			Level l = new Generator().generate(i);
			stringBuilder.append(l).append("\n");
		}
		Debug logger = Debug.getLogger(GeneratorTest.class);
		logger.debug(stringBuilder);
		assertEquals("", stringBuilder.toString());
	}
}
