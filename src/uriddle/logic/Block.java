package uriddle.logic;

public class Block {
  public enum BlockType {
    BOUNDS, DEFAULT, PLAYER, GATE, EXIT,
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
}