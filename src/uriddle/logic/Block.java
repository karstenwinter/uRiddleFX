package uriddle.logic;

public class Block {
  public enum BlockType {
    DEFAULT,
    BOUNDS, PLAYER, GATE, EXIT,
    DOOR, SWITCH,
    PORTAL,
    RYTHM,
    PIXELSPOT,
    MIRRORPLAYER,
    ONEWAY
  }

  public BlockType type;
  public BlockType typeBefore = BlockType.DEFAULT;
  public U bigU;
  public U smallU;
  public Door door;
  public Switch switchVal;
  public Portal portal;

  public Block() {
    this.type = BlockType.DEFAULT;
  }

  @Override
  public String toString() {
    return this.type + " " + bigU + ", " + smallU + ", " + door + ", " + switchVal;
  }

  public Block clone() {
    Block b = new Block();
    b.type = type;
    b.typeBefore = typeBefore;
    b.bigU = bigU;
    b.smallU = smallU;
    b.door = door != null ? door.clone() : null;
    b.switchVal = switchVal;
    b.portal = portal;
    return b;
  }
}