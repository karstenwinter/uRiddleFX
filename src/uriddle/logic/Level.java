package uriddle.logic;

import java.util.*;

public class Level {
  public enum State {
    NO_PLAYER, REACHED_EXIT, MOVED,
    CANNOT_MOVE, NO_GATE_OR_EXIT,
    USED_PORTAL, USED_SWITCH
  }

  public final String id;
  public String name;
  public final List<Row> rows = new ArrayList<Row>();
  public boolean pixelate = false;

  public Level(String id, String name, Row... rows) {
    this.name = name;
    this.id = id;
    this.rows.addAll(Arrays.asList(rows));
  }

  @Override
  public String toString() {
    return LevelWriter.instance.toString(this);
  }

  public State go(Direction dir) {
    return Logic.instance.go(this, dir);
  }

  public Level clone() {
    LevelReader levelReader = LevelReader.instance;
    Level level = levelReader.fromString(toString());
    // door is not persisted and can be invisble
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
    return level;
  }
}