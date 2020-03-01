package uriddle.logic;

public class Door {
  public enum DoorType {
    HORIZONTAL, VERTICAL
  }

  public DoorType type;
  public int num;
  public boolean open = false;

  public Door(DoorType type, int num) {
    this.type = type;
    this.num = num;
  }

  @Override
  public String toString() {
    return "Door " + type + " " + num + " " + open;
  }

  public Door clone() {
    Door door = new Door(type, num);
    door.open = open;
    return door;
  }
}