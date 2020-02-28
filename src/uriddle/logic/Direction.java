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
			throw new AssertionError("unkown Direction " + this);
		}
	}
}
