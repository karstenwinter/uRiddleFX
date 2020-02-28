package uriddle.logic;

import java.util.*;

public class Level {
  public enum State {
    NO_PLAYER, REACHED_EXIT, MOVED, CANNOT_MOVE, NO_GATE_OR_EXIT
  }

  public final String id;
  public final String name;
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
}