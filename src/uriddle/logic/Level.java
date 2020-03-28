package uriddle.logic;

import solid.collectors.ToArray;
import solid.collectors.ToArrayList;
import solid.functions.Func1;
import solid.stream.Stream;

import java.util.*;

import static solid.collectors.ToArray.toArray;
import static solid.collectors.ToArrayList.toArrayList;
import static solid.stream.Stream.stream;

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
    return LevelWriter.instance.toString(this, false);
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
    List<Row> rows = stream(this.rows)
            .map(x -> {
              Block[] blocks = stream(x.cols)
                      .map((Block y) -> y.clone())
                      .collect(toArray(Block.class));
              return new Row(blocks);
            })
            .collect(ToArrayList.<Row>toArrayList());
    // System.out.println(rows.getClass() + " / " + rows.size());
    Level level = new Level(id, name, rows.toArray(new Row[0]));
    level.counter = counter;
    level.pixelate = pixelate;
    return level;
  }
}