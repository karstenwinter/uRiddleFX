package uriddle.logic;

public class Portal {
  public Direction dir;
  public int num;

  public Portal(int num, Direction dir) {
    this.num = num;
    this.dir = dir;
  }

  @Override
  public String toString() {
    return "Portal " + num + " " + dir;
  }
}
