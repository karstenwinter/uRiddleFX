package uriddle.logic;

import uriddle.logic.Block.*;
import uriddle.logic.U.*;

import static uriddle.logic.Door.DoorType.HORIZONTAL;

/**
 * Draws level elements this way:
 *
 * <pre>
 * ##### #####
 * #+++# #+++#
 * #+ +# #+o #
 * #+ +# #+++#
 * #   # #   #
 *
 * ##### #####
 * #+++#  +++#
 * #+ +#  +o #
 * #+ +#  +++#
 * #   # #####
 *
 * +++++ +++++
 *  ###+  ++++
 *  #o +  +  +
 *  ###+  ++++
 * +++++ +++++
 *
 *  \ /    ^
 *   X    <+>
 *  / \    v
 *
 * </pre>
 *
 * @author broxp
 */
public class LevelWriter {
  /**
   * Singleton instance
   */
  public static final LevelWriter instance = new LevelWriter();

  /**
   * The first line is the header line
   */
  public static final int HEADER_LINE = 0;

  // @formatter:off
  static String[] passWay = {
          "## ##",
          "## ##",
          "     ",
          "## ##",
          "## ##"
  };

  static String[][] rythms = {
          {
                  "  +  ",
                  " ++  ",
                  "  +  ",
                  "  +  ",
                  "  +  "
          }, {
          " +++ ",
          "   + ",
          " +++ ",
          " +   ",
          " +++ "
  }, {
          " +++ ",
          "   + ",
          " +++ ",
          "   + ",
          " +++ "
  }, {
          " + + ",
          " + + ",
          " +++ ",
          "   + ",
          "   + "
  }, {
          " +++ ",
          " +   ",
          " +++ ",
          "   + ",
          " +++ "
  }
  };

  static String[] gate = {
          "     ",
          " \\ / ",
          "  X  ",
          " / \\ ",
          "     "
  };

  static String[] doorH = {
          " $$$ ",
          "  $  ",
          "  $  ",
          "  $  ",
          " $$$ "
  };

  static String[] doorV = {
          "     ",
          "$   $",
          "$$$$$",
          "$   $",
          "     "
  };

  static String[] switch1 = {
          "     ",
          "  $  ",
          " $$$ ",
          "     ",
          "     "
  };

  static String[] switch2 = {
          "     ",
          "  $  ",
          "  $$ ",
          "  $  ",
          "     "
  };

  static String[] pixelspot = {
          " PPP ",
          " P P ",
          " PP  ",
          " P   ",
          " P   "
  };

  static String[] exit = {
          "     ",
          "  ^  ",
          " <+> ",
          "  v  ",
          "     "
  };
  static String[][] oneWayNormal = {
          {
                  "     ",
                  "  ^  ",
                  " ^^^ ",
                  " ^ ^ ",
                  "^^ ^^"
          }, {
          ">    ",
          ">>>> ",
          "  >>>",
          ">>>> ",
          ">    "
  }, {
          "vv vv",
          " v v ",
          " vvv ",
          "  v  ",
          "     "
  }, {
          "    <",
          " <<<<",
          "<<<  ",
          " <<<<",
          "    <"
  }
  };
  static String[][] oneWayOnly = {
          {
                  "     ",
                  "  A  ",
                  " AAA ",
                  " A A ",
                  "AA AA"
          }, {
          ")    ",
          ")))) ",
          "  )))",
          ")))) ",
          ")    "
  }, {
          "VV VV",
          " V V ",
          " VVV ",
          "  V  ",
          "     "
  }, {
          "    (",
          " ((((",
          "(((  ",
          " ((((",
          "    ("
  }
  };

  static String[] bounds = {
          ".....",
          ".....",
          ".....",
          ".....",
          "....."
  };
  static String[] portalBottom = {
          ".....",
          ".....",
          ".....",
          "..§..",
          "§§§§§"
  };
  static String[] portalTop = {
          "§§§§§",
          "..§..",
          ".....",
          ".....",
          "....."
  };
  static String[] portalLeft = {
          "§....",
          "§....",
          "§§...",
          "§....",
          "§...."
  };
  static String[] portalRight = {
          "....§",
          "....§",
          "...§§",
          "....§",
          "....§"
  };
  // @formatter:on

  public String toString(Block b) {
    return toString(b, false);
  }

  public String toString(Block b, boolean markBigDifferently) {
    return toString(new Level("", "", new Row(b)), markBigDifferently, false);
  }

  public String toString(Level level) {
    return toString(level, false, false);
  }

  public String toString(Level level, boolean markBigDifferently, boolean forPersistence) {
    StringBuilder res = new StringBuilder();
    StringBuilder small = new StringBuilder();
    StringBuilder big = new StringBuilder();
    int w = 0;
    for (Row row : level.rows) {
      w = Math.max(w, row.cols.size());
    }
    int y = -1;
    int x;
    for (Row row : level.rows) {
      y++;
      if (y == 0) {
        res.append(level.id).append(": ").append(level.name).append("\n");
        insertBlankLine(res, w, row);
        res.append("\n");
      }
      for (int i = 0; i < 5; i++) {
        x = -1;
        for (Block b : row.cols) {
          x++;
          if (x == 0) {
            res.append(" ");
          }
          if (b.bigU != null && b.smallU != null) {
            appendBigAndSmall(res, small, big, i, b, markBigDifferently);
          } else if (b.bigU != null && b.smallU == null) {
            appendBig(res, b.bigU, i, markBigDifferently);
          } else if (b.bigU == null && b.smallU != null) {
            appendSmall(res, b.smallU, i);
          } else if (b.type == BlockType.GATE) {
            res.append(gate[i]);
          } else if (b.type == BlockType.PIXELSPOT) {
            res.append(pixelspot[i]);
          } else if (b.type == BlockType.BOUNDS) {
            res.append(bounds[i]);
          } else if (b.type == BlockType.EXIT) {
            res.append(exit[i]);
          } else if (b.type == BlockType.ONEWAY) {
            Direction dir = b.oneWay.dir;
            //assert (dir != null);
            int id = dir.ordinal();
            //  System.out.println(" " + b.oneWay + " => " + id);
            String[] strings = (b.oneWay.type == OneWay.OneWayType.NOT_REVERSE
                    ? oneWayNormal
                    : oneWayOnly)[id];
            String c = strings[i];

            res.append(c);
          } else if (b.type == BlockType.GLITCH) {
            appendTimes(res, 5, "G");
          } else if (b.type == BlockType.RYTHM) {
            int index = b.num - 1;
            String toWrite = (2 + b.num) + "";
            String str = rythms[index][i];
            if (forPersistence) {
              appendTimes(res, 5, toWrite);
            } else {
              res.append(str.replace(" ", toWrite).replace('+', ' '));
            }

          } else if (b.type == BlockType.PASSWAY) {
            String str = passWay[i];
            res.append(str);
          } else if (b.type == BlockType.PORTAL) {
            String c = (b.portal.dir == Direction.BOTTOM ? portalBottom
                    : b.portal.dir == Direction.LEFT ? portalLeft
                    : b.portal.dir == Direction.RIGHT ? portalRight
                    : portalTop)[i];
            res.append(c.replace("§", (b.num + "")));
          } else if (b.type == BlockType.DOOR && !b.door.open) {
            String str = (b.door.type == HORIZONTAL ? doorH : doorV)[i];
            res.append(str.replace('$', b.num == 1 ? 'd' : 'D'));
          } else if (b.type == BlockType.SWITCH) {
            String str = (b.num == 1 ? switch1 : switch2)[i];
            res.append(str.replace('$', b.num == 1 ? 's' : 'S'));
          } else {
            appendTimes(res, 5, " ");
          }
          if (b.type == BlockType.PLAYER && i == 2) {
            res.replace(res.length() - 3, res.length() - 2, "o");
          }
          if (b.type == BlockType.MIRRORPLAYER && i == 2) {
            res.replace(res.length() - 3, res.length() - 2, "q");
          }
          res.append(" ");
        }
        appendTimes(res, 5 * (w - row.cols.size()), " ");
        res.append("\n");
      }
      insertBlankLine(res, w, row);
      if (y != level.rows.size() - 1) {
        res.append("\n");
      }
    }
    return res.toString();
  }

  void insertBlankLine(StringBuilder res, int w, Row row) {
    appendTimes(res, 5 * w + 1, " ");
    appendTimes(res, row.cols.size(), " ");
  }

  void appendBigAndSmall(StringBuilder res, StringBuilder small,
                         StringBuilder big, int i, Block b,
                         boolean markBigDifferently) {
    small.delete(0, small.length());
    big.delete(0, big.length());
    appendSmall(small, b.smallU, i);
    appendBig(big, b.bigU, i, markBigDifferently);
    for (int part = 0; part < 5; part++) {
      char charAt = small.charAt(part);
      if (charAt == ' ') {
        res.append(big.charAt(part));
      } else {
        res.append(charAt);
      }
    }
  }

  void appendBig(StringBuilder stringBuilder, U u, int i, boolean markBigDifferently) {
    String p = markBigDifferently ?
            getPatternDifferent(u)
            : getPattern(u);
    if (u.dir == Direction.TOP) {
      if (i == 4) {
        appendPattern(stringBuilder, p, 1, 1, 1, 1, 1);
      } else {
        appendPattern(stringBuilder, p, 1, 0, 0, 0, 1);
      }
    }

    if (u.dir == Direction.BOTTOM) {
      if (i == 0) {
        appendPattern(stringBuilder, p, 1, 1, 1, 1, 1);
      } else {
        appendPattern(stringBuilder, p, 1, 0, 0, 0, 1);
      }
    }

    if (u.dir == Direction.LEFT) {
      if (i == 0 || i == 4) {
        appendPattern(stringBuilder, p, 1, 1, 1, 1, 1);
      } else {
        appendPattern(stringBuilder, p, 0, 0, 0, 0, 1);
      }
    }

    if (u.dir == Direction.RIGHT) {
      if (i == 0 || i == 4) {
        appendPattern(stringBuilder, p, 1, 1, 1, 1, 1);
      } else {
        appendPattern(stringBuilder, p, 1, 0, 0, 0, 0);
      }
    }
  }

  void appendSmall(StringBuilder stringBuilder, U u, int i) {
    if (i == 0 || i == 4) {
      appendTimes(stringBuilder, 5, " ");
      return;
    }

    if (u.dir == Direction.TOP) {
      if (i == 3) {
        appendPattern(stringBuilder, getPattern(u), 0, 1, 1, 1, 0);
      } else {
        appendPattern(stringBuilder, getPattern(u), 0, 1, 0, 1, 0);
      }
    }

    if (u.dir == Direction.BOTTOM) {
      if (i == 1) {
        appendPattern(stringBuilder, getPattern(u), 0, 1, 1, 1, 0);
      } else {
        appendPattern(stringBuilder, getPattern(u), 0, 1, 0, 1, 0);
      }
    }

    if (u.dir == Direction.LEFT) {
      if (i == 2) {
        appendPattern(stringBuilder, getPattern(u), 0, 0, 0, 1, 0);
      } else {
        appendPattern(stringBuilder, getPattern(u), 0, 1, 1, 1, 0);
      }
    }

    if (u.dir == Direction.RIGHT) {
      if (i == 2) {
        appendPattern(stringBuilder, getPattern(u), 0, 1, 0, 0, 0);
      } else {
        appendPattern(stringBuilder, getPattern(u), 0, 1, 1, 1, 0);
      }
    }
  }

  void appendPattern(StringBuilder stringBuilder, String pattern, int... a) {
    for (int i : a) {
      appendTimes(stringBuilder, 1, i == 0 ? " " : pattern);
    }
  }

  String getPattern(U u) {
    return u.type == UType.IMPORTANT ? "#" : "+";
  }

  String getPatternDifferent(U u) {
    return u.type == UType.IMPORTANT ? "'" : "+";
  }

  StringBuilder appendTimes(StringBuilder stringBuilder, int times, String p) {
    for (int i = 0; i < times; i++) {
      stringBuilder.append(p);
    }
    return stringBuilder;
  }
}
