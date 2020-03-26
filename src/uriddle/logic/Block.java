package uriddle.logic;

public class Block {

  public enum BlockType {
    BOUNDS, PLAYER, GATE, EXIT,
    DEFAULT,
    DOOR, SWITCH,
    PORTAL,
    RYTHM,
    PASSWAY,
    PIXELSPOT,
    MIRRORPLAYER,
    ONEWAY
  }

  public BlockType type = BlockType.DEFAULT;
  public BlockType typeBefore = BlockType.DEFAULT;
  public U bigU;
  public U smallU;
  public Door door;
  public Switch switchVal;
  public Portal portal;
  public OneWay oneWay;
  public int num;

  public Block(BlockType blockType, U big, U small) {
    this.type = blockType;
    this.bigU = big;
    this.smallU = small;
  }

  public Block() {

  }

  @Override
  public String toString() {
    return this.type + " " + num + " " + bigU + ", " + smallU + ", " + door + ", " + switchVal + ", " + portal + ", " + oneWay;
  }

  public Block clone() {
    Block b = new Block();
    b.num = num;
    b.type = type;
    b.typeBefore = typeBefore;
    b.bigU = bigU;
    b.smallU = smallU;
    b.door = door != null ? door.clone() : null;
    b.switchVal = switchVal;
    b.portal = portal;
    b.oneWay = oneWay;
    return b;
  }
}