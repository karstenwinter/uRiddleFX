package uriddle.logic;

import java.util.*;

public class Row {
	public final List<Block> cols = new ArrayList<Block>();

	public Row(Block... b) {
		cols.addAll(Arrays.asList(b));
	}
}