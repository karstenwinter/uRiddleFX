package uriddle.logic;

import uriddle.logic.Direction;
import uriddle.logic.Level;

import java.util.ArrayList;
import java.util.HashSet;

public class Solver {
  public int maxIt = 50;
  private Direction[] dirs = Direction.values();
  public boolean verbose;

  public Res solve(Level level) {
    Res res = new Res();
    solve(level, res, maxIt);
    return res;
  }

  HashSet<String> visited = new HashSet<String>();
  public static final int solved = -1;

  public Res solve(Level level, Res res, int maxIt) {
    if (res.iterations >= maxIt) {
      return res;//  throw new IllegalArgumentException("Max iterations reached");
    }
    String toS = level.toString();
    if (!visited.add(toS)) {
//            System.out.println("Already visited on step " + res.iterations);
      return res; // already visited
    }

    //System.out.println(toS);
    Level clone = null;
    for (Direction d : dirs) {
      if (clone == null) {
        clone = level.clone();
      }
      Level.State s = Logic.instance.movePlayer(clone, d, Block.BlockType.PLAYER, null);
      if (verbose) {
        System.out.println(clone);
      }
      switch (s) {
        case USED_PORTAL:
          res.usedPortal = true;
          // no break here, it's the same as moved
        case MOVED:
          int x = res.iterations;
          res.iterations++;
          Res subRes = solve(clone, res, maxIt);
          if (subRes.solved) {
            return res;
          }
          res.iterations = x;
          clone = null;
          break;
        case REACHED_EXIT:
          res.solved = true;
          return res;
        case USED_SWITCH:
          res.usedSwitch = true;
          return res;
        case CANNOT_MOVE:
          break;
        default:
          throw new IllegalArgumentException("Invalid Level " + s);
      }
    }
    return res;
  }
}
