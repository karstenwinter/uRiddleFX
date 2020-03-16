package uriddle.logic;

public class Portal {
  public Direction dir;

  public Portal(Direction dir) {
    this.dir = dir;
  }

  @Override
  public String toString() {
    return "Portal " + dir;
  }
}
