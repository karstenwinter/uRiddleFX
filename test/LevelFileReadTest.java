package uriddle.logic.test;

import static uriddle.logic.Direction.*;
import static uriddle.logic.U.UType.*;

import java.util.*;

import junit.framework.*;
import uriddle.logic.*;

public class LevelFileReadTest extends TestCase {

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
