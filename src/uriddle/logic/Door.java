package uriddle.logic;

public class Door {
  public enum DoorType {
    HORIZONTAL, VERTICAL
  }

  public DoorType type;
  public boolean open = false;

  public Door(DoorType type) {
    this.type = type;
  }

  @Override
  public String toString() {
    return "Door " + type + " " + open;
  }

  public Door clone() {
    Door door = new Door(type);
    door.open = open;
    return door;
  }
}