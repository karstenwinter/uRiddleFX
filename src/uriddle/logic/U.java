package uriddle.logic;

public class U {
  public enum UType {
    IMPORTANT, OBSTACLE;
  }

  public UType type;
  public Direction dir;

  public U(UType type, Direction dir) {
    this.type = type;
    this.dir = dir;
  }

  public U clone() {
    return new U(type, dir);
  }

  @Override
  public String toString() {
    return "U" + type + " " + dir;
  }
}