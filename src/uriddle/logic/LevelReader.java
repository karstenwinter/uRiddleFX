package uriddle.logic;

import static uriddle.logic.Block.BlockType.*;
import static uriddle.logic.Direction.*;
import static uriddle.logic.U.UType.*;

import java.util.*;

import uriddle.logic.U.*;

/**
 * Reads level elements, encoded as in {@link LevelWriter}.
 *
 * @author broxp
 */
public class LevelReader {
  public static final LevelReader instance = new LevelReader();
  static final Debug LOG = Debug.getLogger(LevelReader.class);

  public Level fromString(String level) {
    List<String> lines = new ArrayList<String>(
            Arrays.asList(level.split("\n")));

    if (lines.size() == 0) {
      throw new IllegalArgumentException("Level was empty");
    }

    String first = lines.remove(LevelWriter.HEADER_LINE);
    int indexOfColon = first.indexOf(": ");
    if (indexOfColon == -1) {
      throw new IllegalArgumentException(
              "First line must have format <id: name>, was <" + first + ">" + level);
    }
    String id = first.substring(0, indexOfColon);
    String name = first.substring(indexOfColon + 2);

    int textWidth = -1;
    for (String line : lines) {
      if (textWidth == -1) {
        textWidth = line.length();
      }
      if (textWidth != line.length()) {
        throw new IllegalArgumentException(
                "All lines must have the same length. Line <" + line
                        + "> was not of length " + textWidth + level);
      }
    }

    int w = checkDivisible(textWidth, 1, 6, "All line lengths" + level);
    int h = checkDivisible(lines.size() + 1, 2, 6, "Number of lines" + level);

    //LOG.debug("w%s h%s", w, h);
    Level res = new Level(id, name);
    for (int y = 0; y < h; y++) {
      Row row = new Row();
      res.rows.add(row);
      for (int x = 0; x < w; x++) {
        Block block = new Block();
        int midY = 1 + y * 6 + 2;
        int midX = 1 + x * 6 + 2;
        char centerChar = lines.get(midY).charAt(midX);
        //LOG.debug("Block@y%sx%s, Char@y%sx%s: %s ", y, x, midY, midX, centerChar);

        char uDelta1 = lines.get(midY - 1).charAt(midX);
        char lDelta1 = lines.get(midY).charAt(midX - 1);
        char rDelta1 = lines.get(midY).charAt(midX + 1);
        char dDelta1 = lines.get(midY + 1).charAt(midX);

        char uDelta2 = lines.get(midY - 2).charAt(midX);
        char lDelta2 = lines.get(midY).charAt(midX - 2);
        char rDelta2 = lines.get(midY).charAt(midX + 2);
        char dDelta2 = lines.get(midY + 2).charAt(midX);

        char diagon1 = lines.get(midY - 1).charAt(midX - 1);
        char diagon2 = lines.get(midY - 1).charAt(midX + 1);
        char diagon3 = lines.get(midY + 1).charAt(midX - 1);
        char diagon4 = lines.get(midY + 1).charAt(midX + 1);

        if (centerChar == '.') {
          if (uDelta2 != '.' || lDelta2 != '.' || rDelta2 != '.' || dDelta2 != '.') {
            block.type = PORTAL;
            block.portal = new Portal(getDir(uDelta2, rDelta2, dDelta2, lDelta2));
            block.num = uDelta2 == '1' || lDelta2 == '1' || rDelta2 == '1' || dDelta2 == '1'
                    ? 1 : 2;
          } else {
            block.type = BOUNDS;
          }
        } else if (centerChar == 'X') {
          block.type = GATE;
        } else if (centerChar == '+') {
          block.type = EXIT;
        } else if (centerChar == 'o') {
          block.type = PLAYER;
        } else if (centerChar == 'q') {
          block.type = MIRRORPLAYER;
        } else if (centerChar == 'd' || centerChar == 'D') {
          block.type = DOOR;
          block.door = new Door(rDelta2 != ' ' ? Door.DoorType.VERTICAL : Door.DoorType.HORIZONTAL);
          block.num = centerChar == 'd' ? 1 : 2;
        } else if (centerChar == 's' || centerChar == 'S') {
          block.type = SWITCH;
          block.switchVal = new Switch(Switch.SwitchType.TOGGLE);
          block.num = centerChar == 's' ? 1 : 2;
        } else if (centerChar == 'p' || centerChar == 'P') {
          block.type = PIXELSPOT;
        } else if (centerChar == 'g' || centerChar == 'G') {
          block.type = GLITCH;
        } else if (centerChar == '3' || centerChar == '4' || centerChar == '5' || centerChar == '6' || centerChar == '7') {
          block.type = RYTHM;
          block.num = Integer.parseInt(centerChar + "") - 2;
          res.maxCounter = Math.max(block.num, res.maxCounter);
        } else if (centerChar == '<' || centerChar == '>' || centerChar == 'v' || centerChar == '^') {
          block.type = ONEWAY;
          block.oneWay = new OneWay(OneWay.OneWayType.NOT_REVERSE,
                  getDir(uDelta1, rDelta1, dDelta1, lDelta1).opposite());
        } else if (centerChar == '(' || centerChar == ')' || centerChar == 'V' || centerChar == 'A') {
          block.type = ONEWAY;
          block.oneWay = new OneWay(OneWay.OneWayType.ONLY_DIRECTION,
                  getDir(uDelta1, rDelta1, dDelta1, lDelta1).opposite());
        } else if (centerChar == ' '
                && diagon1 == '#' && diagon2 == '#' && diagon3 == '#' && diagon4 == '#'
                && uDelta1 == ' ' && rDelta1 == ' ' && dDelta1 == ' ' && lDelta1 == ' ') {
          block.type = PASSWAY;
        }

        if (block.type == DEFAULT || block.type == PLAYER) {

          Direction dirDelta1 = getDir(uDelta1, rDelta1, dDelta1, lDelta1);
          if (dirDelta1 != null) {
            UType type = getUType(uDelta1, rDelta1, dDelta1, lDelta1);
            block.smallU = new U(type, dirDelta1);
          }

          Direction dirDelta2 = getDir(uDelta2, rDelta2, dDelta2, lDelta2);
          if (dirDelta2 != null) {
            UType type = getUType(uDelta2, rDelta2, dDelta2, lDelta2);
            block.bigU = new U(type, dirDelta2);
          }
        }

        row.cols.add(block);
      }
    }

    return res;
  }

  // @formatter:off
  public UType getUType(char u, char r, char d, char l) {
    return
            u == '#' || r == '#' || u == '#' || l == '#' ? IMPORTANT :
                    u == '+' || r == '+' || u == '+' || l == '+' ? OBSTACLE :
                            null;
  }

  public Direction getDir(char u, char r, char d, char l) {
    Direction direction = !isBlock(u) && isBlock(r) && isBlock(d) && isBlock(l) ? TOP :
            isBlock(u) && !isBlock(r) && isBlock(d) && isBlock(l) ? RIGHT :
                    isBlock(u) && isBlock(r) && !isBlock(d) && isBlock(l) ? BOTTOM :
                            isBlock(u) && isBlock(r) && isBlock(d) && !isBlock(l) ? LEFT :
                                    null;
    // assert (direction != null);
    return direction;
  }

  public boolean isBlock(char c) {
    return c != ' ' && !(c == '1' || c == '2');
  }

  int checkDivisible(int val, int sub, int div, String prefix) {
    if ((val - sub) % div != 0) {
      throw new IllegalArgumentException(prefix + " minus " + sub
              + " must be divisble by " + div + ", was:  " + val);
    }
    return (val - sub) / div;
  }
}
