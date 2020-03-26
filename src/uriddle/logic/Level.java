package uriddle.logic;

import javafx.util.Pair;

import java.util.*;
import java.util.stream.Collectors;

public class Level {
  public enum State {
    REACHED_EXIT,
    OPENED_EXIT, USED_PORTAL, MOVED,
    CANNOT_MOVE,
    USED_SWITCH,
    NO_PLAYER, NO_GATE_OR_EXIT
  }

  // public final long ts = System.currentTimeMillis();
  public String id;
  public String name;
  public final List<Row> rows = new ArrayList<Row>();
  public boolean pixelate = false;
  public int counter = 1;

  public Level(String id, String name, Row... rows) {
    this.name = name;
    this.id = id;
    this.rows.addAll(Arrays.asList(rows));
  }

  @Override
  public String toString() {
    return LevelWriter.instance.toString(this);
  }

  public Level clone() {
    /*LevelReader levelReader = LevelReader.instance;
    Level level = levelReader.fromString(toString());
    level.counter = counter;
    // door may be not persisted in string / can be invisble
    int y = -1;
    for (Row r : rows) {
      y++;
      int x = -1;
      for (Block b : r.cols) {
        x++;
        if (b.type == Block.BlockType.DOOR) {
          Block block = level.rows.get(y).cols.get(x);
          block.type = b.type;
          block.door = b.door.clone();
        }
      }
    }
    return level;*/
    List<Row> rows = this.rows.stream()
            .map(x -> new Row(x.cols.stream()
                    .map(y -> y.clone()).collect(Collectors.toList()).toArray(new Block[0])))
            .collect(Collectors.toList());
    // System.out.println(rows.getClass() + " / " + rows.size());
    return new Level(id, name, rows.toArray(new Row[0]));
  }
}