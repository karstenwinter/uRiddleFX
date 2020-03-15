package uriddle.logic;

public class OneWay {
  public enum OneWayType {
    NOT_REVERSE, ONLY_DIRECTION
  }

  public Direction dir;
  public OneWayType type;

  public OneWay(OneWayType type, Direction dir) {
    this.type = type;
    this.dir = dir;
  }

  @Override
  public String toString() {
    return "OneWay " + type + " " + dir;
  }
}
