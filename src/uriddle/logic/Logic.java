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

        int comp = Integer.valueOf(state.ordinal()).compareTo(Integer.valueOf(state2.ordinal()));
        if (comp < 0) {
            return state;
        } else {
            return state2;
        }
    }

    State movePlayer(Level level, Direction dir, Block.BlockType playerType) {
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

                            if (c.portal != null && targetBlock.portal.num != c.portal.num) {
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
                boolean moved = doMove(dir, playerBlock, targetBlock, level, playerType, false,
                        fromPortalDir, toPortalDir);
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
                   boolean checkOnly, Direction fromPortalDir, Direction toPortalDir) {
        Direction enterDir = toPortalDir == null ? inputDir : toPortalDir;
        Direction moveAwayDir = fromPortalDir == null ? inputDir : fromPortalDir.opposite();

        boolean playerHadSmallU = player.smallU != null;
        boolean playerHadBigU = player.bigU != null;

        if (target.type == BOUNDS || target.type == GATE || (target.type == DOOR && !target.door.open)) {
            LOG.debug("cannot go on blocked " + target);
            return false;
        }

        if (target.smallU != null && target.smallU.dir.opposite() != enterDir) {
            LOG.debug("cannot go into small u");
            return false;
        }

        if (target.bigU != null && target.bigU.dir.opposite() != enterDir) {
            LOG.debug("cannot go into big u");
            return false;
        }

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
                            c.door.open = c.door.num == target.switchVal.num;
                    }
                }
            }
            if (target.type == PIXELSPOT) {
                level.pixelate = !level.pixelate;
            }

            player.type = player.typeBefore;

            target.typeBefore = target.type;
            target.type = playerType;

            changeTargetIfNeeded(playerHadSmallU, playerHadBigU, target, fromPortalDir, toPortalDir);
        }
        return true;
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
