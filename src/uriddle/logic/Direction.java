package uriddle.logic;

public enum Direction {
  TOP(0, -1), RIGHT(1, 0), BOTTOM(0, 1), LEFT(-1, 0);
  public final int dx, dy;

  private Direction(int dx, int dy) {
    this.dx = dx;
    this.dy = dy;
  }

  public Direction opposite() {
    switch (this) {
      case LEFT:
        return RIGHT;
      case RIGHT:
        return LEFT;
      case BOTTOM:
        return TOP;
      case TOP:
        return BOTTOM;
      default:
        throw new AssertionError("unknown Direction " + this);
    }
  }

  /**
   * can not be called with opposite dirs
   */
  public boolean getTurnIsClockwise(Direction to) {
    switch (this) {
      case LEFT:
        return to == TOP;
      case RIGHT:
        return to == BOTTOM;
      case BOTTOM:
        return to == LEFT;
      case TOP:
        return to == RIGHT;
      default:
        throw new AssertionError("unknown Direction " + this);
    }
  }

  public Direction turn(boolean clockwise) {
    switch (this) {
      case LEFT:
        return clockwise ? TOP : BOTTOM;
      case RIGHT:
        return clockwise ? BOTTOM : TOP;
      case BOTTOM:
        return clockwise ? LEFT : RIGHT;
      case TOP:
        return clockwise ? RIGHT : LEFT;
      default:
        throw new AssertionError("unknown Direction " + this);
    }
  }
}
