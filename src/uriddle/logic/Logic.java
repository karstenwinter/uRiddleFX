package uriddle.logic;

import static uriddle.logic.Block.BlockType.*;
import static uriddle.logic.Level.State.*;
import static uriddle.logic.U.UType.*;

import java.util.AbstractMap.*;
import java.util.Map;
import java.util.Map.*;

import uriddle.logic.Level.*;

public class Logic {
  static final Debug LOG = Debug.getLogger(Logic.class);

  public static final Logic instance = new Logic();

  public State go(Level level, Direction dir) {
    State state = movePlayer(level, dir, PLAYER);
    State state2 = movePlayer(level, dir.opposite(), MIRRORPLAYER);

    return state == null ? state2 : state == REACHED_EXIT ? state : state2;
  }

  private State movePlayer(Level level, Direction dir, Block.BlockType playerType) {
    Entry<Block, int[]> pair = getPlayerWithPos(level, playerType);
    if (pair == null) {
      return NO_PLAYER;
    }
    Block playerBlock = pair.getKey();
    int y = pair.getValue()[0];
    int x = pair.getValue()[1];

    int targetY = y + dir.dy;
    int targetX = x + dir.dx;
    LOG.debug("from Y%sX%s to Y%sX%s", y, x, targetY, targetX);

    Block targetBlock = null;
    State res = CANNOT_MOVE;
    if (targetY < 0 || targetY >= level.rows.size()) {
      LOG.debug("target pos out of bounds");
    } else {
      Row targetRow = level.rows.get(targetY);
      if (targetX < 0 || targetX >= targetRow.cols.size()) {
        LOG.debug("target pos out of bounds");
      } else {

        targetBlock = targetRow.cols.get(targetX);
        if (targetBlock.portal != null) {
          LOG.debug("target is portal " + targetBlock.portal);
          int portalY = -1;
          outer:
          for (Row r : level.rows) {
            portalY++;
            int portalX = -1;
            for (Block c : r.cols) {
              portalX++;

              if (c.portal != null && targetBlock.portal.num != c.portal.num) {
                targetBlock = c;
                int dx = targetBlock.portal.dir.dx;
                int dy = targetBlock.portal.dir.dy;
                targetBlock = level.rows.get(portalY + dy).cols.get(portalX + dx);

                LOG.debug("new target behind portal: " + targetBlock + " @ Y" + (portalY + dy) + "X" + (portalX + dx));
                break outer;
              }
            }
          }
        }

        boolean isExit = targetBlock.type == EXIT;

        boolean moved = doMove(dir, playerBlock, targetBlock, level, playerType);
        if (moved) {
          res = MOVED;

          if (targetBlock.bigU != null && targetBlock.smallU != null
                  && targetBlock.bigU.type == IMPORTANT
                  && targetBlock.smallU.type == IMPORTANT) {
            LOG.debug("opened exit!");
            boolean found = false;
            for (Row row : level.rows) {
              for (Block block : row.cols) {
                if (block.type == GATE || block.type == EXIT) {
                  found = true;
                  block.type = EXIT;
                }
              }
            }
            if (!found) {
              res = NO_GATE_OR_EXIT;
            }
          }
          if (isExit) {
            res = REACHED_EXIT;
          }
        }
      }
    }

    // LOG.debug(LevelWriter.instance.toString(player));
    // LOG.debug(dir);
    // LOG.debug(targetBlock == null ? "out of level"
    // : LevelWriter.instance.toString(targetBlock));
    // LOG.debug(res);

    return res;
  }

  boolean doMove(Direction dir, Block player, Block target, Level level, Block.BlockType playerType) {
    if (target.type == BOUNDS || target.type == GATE || (target.type == DOOR && !target.door.open)) {
      LOG.debug("cannot go on blocked " + target);
      return false;
    }

    if (target.smallU != null && target.smallU.dir.opposite() != dir) {
      LOG.debug("cannot go into small u");
      return false;
    }

    if (target.bigU != null && target.bigU.dir.opposite() != dir) {
      LOG.debug("cannot go into big u");
      return false;
    }

    if (player.bigU != null) {
      Direction bigUDir = player.bigU.dir;
      LOG.debug("player has big u open to " + bigUDir);
      if (dir != bigUDir) {
        LOG.debug("would be moved to target");
        if (target.bigU == null && target.smallU == null) {
          LOG.debug("which is possible");
          // move it
          target.bigU = player.bigU;
          player.bigU = null;

          target.smallU = player.smallU;
          player.smallU = null;
        } else {
          LOG.debug("which is not possible");
          return false;
        }
      } else {
        LOG.debug("would be left");
      }
    }

    if (player.smallU != null) {
      Direction smallUDir = player.smallU.dir;
      LOG.debug("player has small u open to %s", smallUDir);
      if (dir != smallUDir) {
        LOG.debug("would be moved to target");
        if (target.smallU == null) {
          LOG.debug("which has no small u");
          boolean ok = true;
          if (target.bigU == null) {
            LOG.debug("and no big u, so it's possible");
          } else if (target.bigU.dir.opposite() == dir) {
            LOG.debug("and a big u opened to %s, so it's possible", dir);
          } else {
            ok = false;
          }

          if (ok) {
            target.smallU = player.smallU;
            player.smallU = null;
          } else {
            return false;
          }
        } else {
          LOG.debug("which is not possible");
          return false;
        }
      } else {
        LOG.debug("would be left");
      }
    }

    if (target.switchVal != null) {
      level.rows.forEach(r ->
      {
        r.cols.forEach(c ->
        {
          if (c.door != null)
            c.door.open = c.door.num == target.switchVal.num;
        });
      });
    }
    if (target.type == PIXELSPOT) {
      level.pixelate = !level.pixelate;
    }

    player.type = player.typeBefore;

    target.typeBefore = target.type;
    target.type = playerType;

    return true;
  }

  Entry<Block, int[]> getPlayerWithPos(Level level, Block.BlockType playerType) {
    int y = 0;
    int x;
    for (Row row : level.rows) {
      x = 0;
      for (Block b : row.cols) {
        if (b.type == playerType) {
          return new SimpleImmutableEntry<Block, int[]>(b, new int[]{y, x});
        }
        x++;
      }
      y++;
    }
    return null;
  }
}
