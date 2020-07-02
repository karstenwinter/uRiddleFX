package test;

import static uriddle.logic.Direction.*;
import static uriddle.logic.U.UType.*;

import java.util.*;

import junit.framework.*;
import uriddle.logic.*;

public class ReaderTest extends TestCase {
	U noBig = null;
	U noSmall = null;

	public void testRead() throws Exception {
		LevelReader rd = LevelReader.instance;

		assertEquals(null, rd.getDir('+', '+', '+', '+'));
		assertEquals(LEFT, rd.getDir('+', '+', '+', ' '));
		assertEquals(BOTTOM, rd.getDir('+', '+', ' ', '+'));
		assertEquals(null, rd.getDir('#', '#', '#', '#'));
		assertEquals(BOTTOM, rd.getDir('#', '#', ' ', '#'));

		assertEquals(IMPORTANT, rd.getUType('#', '#', ' ', '#'));
		assertEquals(OBSTACLE, rd.getUType('+', ' ', '+', '+'));

		Level sampleLevel = Game.get(Game.getLevels(), 3);
		Level parsed = rd.fromString(sampleLevel.toString());
		assertEquals(sampleLevel.toString(), parsed.toString());
	}

	public void testReadTxt() throws Exception {
		List<String> levels = Game.getLevels();
		for (String entry : levels) {
			String string = LevelReader.instance.fromString(entry).toString();
			System.out.println(string);
			assertEquals(entry, string);
		}
		assertNotSame(0, levels.size());
	}
}
