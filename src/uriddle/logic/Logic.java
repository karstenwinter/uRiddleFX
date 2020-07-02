package uriddle.logic;

import static solid.collectors.ToArrayList.toArrayList;
import static solid.stream.Stream.stream;
import static uriddle.logic.Block.BlockType.*;
import static uriddle.logic.Level.State.*;
import static uriddle.logic.U.UType.*;

import java.util.AbstractMap;
import java.util.AbstractMap.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.*;

import solid.collectors.ToArrayList;
import solid.functions.Func1;
import solid.functions.Func2;
import solid.stream.Stream;
import uriddle.logic.Level.*;

public class Logic {
  static final Debug LOG = Debug.getLogger(Logic.class);

  public static final Logic instance = new Logic();

  public Map.Entry<State, String[]> goWithAnimation(Level level, Direction dir) {
    ArrayList<String> list = new ArrayList<String>(5);
    State go = Logic.instance.go(level, dir, list);
    return new AbstractMap.SimpleEntry<State, String[]>(go, list.toArray(new String[5]));
  }

  public State go(Level level, Direction dir) {
    return go(level, dir, null);
  }

  public State go(Level level, Direction dir, ArrayList<String> animation) {
    State state = movePlayer(level, dir, PLAYER, animation);
    State state2 = movePlayer(level, dir.opposite(), MIRRORPLAYER, animation);

    int comp = Integer.valueOf(state.ordinal()).compareTo(state2.ordinal());
    if (state != CANNOT_MOVE) {
      level.counter += 1;
      if (level.counter > 5) {
        level.counter = 1;
      }
      System.out.println("Counter " + level.counter);
    }
    if (comp < 0) {
      return state;
    } else {
      return state2;
    }
  }

  State movePlayer(Level level, Direction dir, Block.BlockType playerType, ArrayList<String> animation) {
    Entry<Block, int[]> pair = getPlayerWithPos(level, playerType);
    if (pair == null) {
      return NO_PLAYER;
    }
    //Direction originalDir = dir;
    Block playerBlock = pair.getKey();
    int y = pair.getValue()[0];
    int x = pair.getValue()[1];

    int targetY = y + dir.dy;
    int targetX = x + dir.dx;
    LOG.debug("from Y%sX%s to Y%sX%s", y, x, targetY, targetX);

    Block targetBlock;
    State res = CANNOT_MOVE;

    boolean targetWasPortal = false;
    if (targetY < 0 || targetY >= level.rows.size()) {
      LOG.debug("target pos out of bounds");
    } else {
      Row targetRow = level.rows.get(targetY);
      if (targetX < 0 || targetX >= targetRow.cols.size()) {
        LOG.debug("target pos out of bounds");
      } else {
        Direction fromPortalDir = null;
        Direction toPortalDir = null;

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

              if (c.portal != null && targetBlock.num != c.num) {
                fromPortalDir = targetBlock.portal.dir;
                Block targetPortal = c;
                targetWasPortal = true;
                // dir = targetPortal.portal.dir;
                toPortalDir = targetPortal.portal.dir;
                LOG.debug("target portal at Y" + portalY + "X" + portalX);

                int dx = toPortalDir.dx;
                int dy = toPortalDir.dy;
                targetBlock = level.rows.get(portalY + dy).cols.get(portalX + dx);
                LOG.debug("new target behind portal: " + targetBlock + " @ Y" + (portalY + dy) + "X" + (portalX + dx));
                break outer;
              }
            }
          }
        }

        boolean isExit = targetBlock.type == EXIT;

        /*if (targetWasPortal) {
          boolean canMove = doMove(dir, playerBlock, targetBlock, level, playerType, true);
          if (!canMove) {
            return CANNOT_MOVE;
          }
        }*/
        boolean moved = doMove(
                dir, playerBlock, targetBlock, level, playerType, false,
                fromPortalDir, toPortalDir, level.counter, animation);
        if (moved) {
          res = targetWasPortal ? USED_PORTAL : MOVED;

          if (targetBlock.bigU != null && targetBlock.smallU != null
                  && targetBlock.bigU.type == IMPORTANT
                  && targetBlock.smallU.type == IMPORTANT) {
            LOG.debug("opened exit!");
            boolean found = false;
            for (Row row : level.rows) {
              for (Block block : row.cols) {
                if (block.type == GATE || block.type == EXIT) {
                  found = true;
                  if (block.type == GATE) {
                    res = OPENED_EXIT;
                  }
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

  void changeDirs(boolean playerHadSmallU, boolean playerHadBigU, Block block, boolean opposite, boolean clockwiseChange) {
    if (opposite) {
      LOG.debug("change block into oppsite dirs" + block);
      if (playerHadSmallU && block.smallU != null) {
        block.smallU.dir = block.smallU.dir.opposite();
      }
      if (playerHadBigU && block.bigU != null) {
        block.bigU.dir = block.bigU.dir.opposite();
      }
      LOG.debug("after: " + block);
    } else {
      LOG.debug("change block into oppsite dirs with clockwise  " + clockwiseChange + ": " + block);
      if (playerHadSmallU && block.smallU != null) {
        block.smallU.dir = block.smallU.dir.turn(clockwiseChange);
      }
      if (playerHadBigU && block.bigU != null) {
        block.bigU.dir = block.bigU.dir.turn(clockwiseChange);
      }
      LOG.debug("after: " + block);
    }
  }

  boolean doMove(Direction inputDir, Block player, Block target, Level level, Block.BlockType playerType,
                 boolean checkOnly, Direction fromPortalDir, Direction toPortalDir, int counter,
                 ArrayList<String> animation) {
    Direction enterDir = toPortalDir == null ? inputDir : toPortalDir;
    Direction moveAwayDir = fromPortalDir == null ? inputDir : fromPortalDir.opposite();

    boolean playerHadSmallU = player.smallU != null;
    boolean playerHadBigU = player.bigU != null;

    if (target.type == BOUNDS || target.type == GATE
            || (target.type == DOOR && !target.door.open)) {
      LOG.debug("cannot go on blocked " + target);
      return false;
    }

    if (target.type == ONEWAY) {
      if (target.oneWay.type == OneWay.OneWayType.ONLY_DIRECTION
              && target.oneWay.dir != enterDir) {
        return false;
      }
      if (target.oneWay.type == OneWay.OneWayType.NOT_REVERSE
              && target.oneWay.dir.opposite() == enterDir) {
        return false;
      }
    }

    if (target.type == RYTHM && target.num == counter) {
      return false;
    }

    //if (target.type == PASSWAY &&player.bigU != null && player.bigU.dir.opposite()!=moveAwayDir) {
    //  return false;
    //}

    if (target.smallU != null && target.smallU.dir.opposite() != enterDir) {
      LOG.debug("cannot go into small u");
      return false;
    }

    if (target.bigU != null && target.bigU.dir.opposite() != enterDir) {
      LOG.debug("cannot go into big u");
      return false;
    }

    Block playerBefore = player.clone();
    Block targetBefore = target.clone();

    if (player.bigU != null) {
      Direction bigUDir = player.bigU.dir;
      LOG.debug("player has big u open to " + bigUDir);
      if (moveAwayDir != bigUDir) {
        LOG.debug("would be moved to target");
        if (target.bigU == null && target.smallU == null) {
          LOG.debug("which is possible");
          if (!checkOnly) {
            // move it
            target.bigU = player.bigU;
            player.bigU = null;

            target.smallU = player.smallU;
            player.smallU = null;
          }
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
      if (moveAwayDir != smallUDir) {
        LOG.debug("would be moved to target");
        if (target.smallU == null) {
          LOG.debug("which has no small u");
          boolean ok = true;
          if (target.bigU == null) {
            LOG.debug("and no big u, so it's possible");
          } else if (target.bigU.dir.opposite() == enterDir) {
            LOG.debug("and a big u opened to %s, so it's possible", enterDir);
          } else {
            ok = false;
          }

          if (ok) {
            if (!checkOnly) {
              // move it
              target.smallU = player.smallU;
              player.smallU = null;
            }
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
    if (!checkOnly) {
      if (target.switchVal != null) {
        for (Row r : level.rows) {
          for (Block c : r.cols) {
            if (c.door != null)
              c.door.open = c.num == target.num;
          }
        }
      }
      if (target.type == PIXELSPOT) {
        level.pixelate = !level.pixelate;
      }
      // move it
      player.type = player.typeBefore;

      target.typeBefore = target.type;
      target.type = playerType;

      changeTargetIfNeeded(playerHadSmallU, playerHadBigU, target, fromPortalDir, toPortalDir);

      // animate
      animate(animation, level, playerBefore, targetBefore, player, target, enterDir, moveAwayDir);
    }
    return true;
  }

  void animate(ArrayList<String> animation, Level level,
               Block playerBefore, Block targetBefore,
               Block player, Block target, Direction enterDir, Direction moveAwayDir) {
    if (animation == null) {
      return;
    }
    int y = -1;
    int playerPosX = -1, playerPosY = -1, targetPosX = -1, targetPosY = -1;
    for (Row r : level.rows) {
      y++;
      int x = -1;
      for (Block b : r.cols) {
        x++;
        if (b == player) {
          playerPosX = x;
          playerPosY = y;
        }
        if (b == target) {
          targetPosX = x;
          targetPosY = y;
        }
      }
    }

    System.out.println("playerPos yx " + playerPosY + "," + playerPosX
            + " targetPos yx " + targetPosY + "," + targetPosX);
    Level l = level.clone();
    String after = LevelWriter.instance.toString(l, true, false);
    Block blockForPlayer = player.clone();
    blockForPlayer.type = DEFAULT; // remove player
    l.rows.get(playerPosY).cols.set(playerPosX, blockForPlayer);

    Block blockForTarget = targetBefore.clone();
    blockForTarget.type = DEFAULT; // remove player
    l.rows.get(targetPosY).cols.set(targetPosX, blockForTarget);
    String before = LevelWriter.instance.toString(l, true, false);
    //animation.add(before);
    System.out.println(before);
    //l.rows.get(playerPosY).cols.set(playerPosX, new Block(DEFAULT, null, null));
    //l.rows.get(targetPosY).cols.set(targetPosX, new Block(DEFAULT, null, null));
    //before = l.toString();

    char[][] gridBefore = as2dGrid(before);

    int h = gridBefore.length;
    int w = gridBefore[0].length;

    char[][] playerBeforeGrid = as2dGrid(LevelWriter.instance.toString(playerBefore, true));
    char[][] targetGrid = as2dGrid(LevelWriter.instance.toString(target, true));

    int shiftMax = 5;
    for (int shift = 1; shift <= shiftMax; shift++) {
      if (shift <= 4) {
        char[][] grid = new char[h][w];

        for (int gridY = 0; gridY < h; gridY++) {
          for (int gridX = 0; gridX < w; gridX++) {

            char value = gridBefore[gridY][gridX];

            boolean isPlayerCoord = isBlockCoordForGridCoord(playerPosY, playerPosX, gridY, gridX);
            boolean isTargetCoord = isBlockCoordForGridCoord(targetPosY, targetPosX, gridY, gridX);
            //System.out.println("grid yx " + gridY + "," + gridX + ": player? " + isPlayerCoord + ", target? " + isTargetCoord);
            //char value = gridBefore[gridY - moveAwayDir.dy * shift][gridX - moveAwayDir.dx * shift];
            //if (true) { //gridX % 6 != 0 && gridY % 6 != 0) {
            if (isPlayerCoord) {
              int shiftedCoordY = (gridY % 6 - moveAwayDir.dy * shift);
              int shiftedCoordX = (gridX % 6 - moveAwayDir.dx * shift);
              value = getCharToMove(true, player, playerBefore, target, targetBefore,
                      getOrBlank(playerBeforeGrid, shiftedCoordY, shiftedCoordX), value);// 'P';
            }

            if (isTargetCoord) {
              int shiftedCoordY = (gridY % 6 + enterDir.dy * (shiftMax - shift));
              int shiftedCoordX = (gridX % 6 + enterDir.dx * (shiftMax - shift));
              value = getCharToMove(false, player, playerBefore, target, targetBefore,
                      getOrBlank(targetGrid, shiftedCoordY, shiftedCoordX), value); // 'T';
            }
            //}

            grid[gridY][gridX] = value;
          }
        }

        animation.add(stream(grid)
                .map(row -> new String(row))
                .reduce(": ", (a, b) -> a + "\n" + b));
        // gridBefore = grid;
      }
    }
    //   l.rows.get(playerPosY).cols.set(playerPosX, player);
    // l.rows.get(targetPosY).cols.set(targetPosX, target);
    animation.add(after);
  }

  char getCharToMove(boolean playerBlock, Block player, Block playerBefore,
                     Block target, Block targetBefore,
                     char current,
                     char currentOriginal) {
    if (playerBlock) {
      char small = uCase(player.smallU, playerBefore.smallU, current, false);
      if (small != 0) {
        return small;
      }
      char big = uCase(player.bigU, playerBefore.bigU, current, true);
      if (big != 0) {
        return big;
      }
    } else {
      char small = uCase(target.smallU, targetBefore.smallU, current, false);
      if (small != 0) {
        return small;
      }
      char big = uCase(target.bigU, targetBefore.bigU, current, true);
      if (big != 0) {
        return big;
      }
    }
    return currentOriginal;
  }

  char uCase(U now, U before, char current, boolean big) {
    if (current == 'o') {
      return current;
    }
    if (now != null && before == null) {
      if (current == (now.type == IMPORTANT ? big ? '\'' : '#' : big ? '-' : '+'))
        return current;
    }
    if (before != null && now == null) {
      if (current == (before.type == IMPORTANT ? big ? '\'' : '#' : big ? '-' : '+'))
        return current;
    }
    return 0;
  }

  char getOrBlank(char[][] grid, int y, int x) {
    if (y < 0 || y >= grid.length)
      return ' ';
    char[] chars = grid[y];
    if (x < 0 || x >= chars.length)
      return ' ';
    return chars[x];
  }

  private char[][] as2dGrid(String levelString) {
    char[][] gridBefore = new char[0][0];
    String[] stringRows = levelString.split("\n");
    gridBefore = stream(stringRows)
            .skip(1)
            .map((Func1<String, Object>) row -> {
              char[] chars = row.toCharArray();
              return chars;
            })
            .collect(toArrayList())
            .toArray(gridBefore);
    return gridBefore;
  }

  public int delta = 0;
  public int delta2 = 0;

  boolean isBlockCoordForGridCoord(int blockPosY, int blockPosX, int gridY, int gridX) {
    int x = gridX / 6;
    int y = gridY / 6;
    return x == blockPosX && y == blockPosY;
  }

  void changeTargetIfNeeded(boolean playerHadSmallU, boolean playerHadBigU, Block target, Direction fromPortalDir, Direction toPortalDir) {
    if (fromPortalDir != null && toPortalDir != null && fromPortalDir != toPortalDir) {
      changeDirs(playerHadSmallU, playerHadBigU, target,
              fromPortalDir == toPortalDir.opposite(),
              fromPortalDir.getTurnIsClockwise(toPortalDir));
    }
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
