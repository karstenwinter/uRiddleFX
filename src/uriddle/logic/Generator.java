package uriddle.logic;

import static uriddle.logic.Block.BlockType.*;
import static uriddle.logic.U.UType.*;

import java.util.*;

import uriddle.logic.Block.*;
import uriddle.logic.U.*;

public class Generator {
  public Level generate(int seed) {
    Random random = new Random(seed);
    int h = random.nextInt(3) + 4;
    int w = random.nextInt(3) + 4;

    int py = random.nextInt(h - 2) + 1;
    int px = random.nextInt(w - 2) + 1;

    int ey = h - 1;
    int ex = w - 1;
    if (random.nextBoolean()) {
      ey = random.nextInt(h - 2) + 1;
    } else {
      ex = random.nextInt(w - 2) + 1;
    }

    int ky = -1;
    int kx = -1;
    boolean kBig = false;

    Direction[] values = Direction.values();
    Level level = new Level("gen" + seed, "gen" + seed);
    U[] us = new U[2];
    for (int y = 0; y < h; y++) {
      Row row = new Row();
      level.rows.add(row);
      for (int x = 0; x < w; x++) {
        BlockType t = DEFAULT;
        if (x == px && y == py) {
          t = PLAYER;
        } else if (x == ex && y == ey) {
          t = GATE;
        } else if (x == 0 || x == w - 1 || y == 0 || y == h - 1) {
          t = BOUNDS;
        }

        if (t == DEFAULT && random.nextBoolean()) {
          int i = random.nextInt(2);
          // boolean imp = random.nextBoolean();
          // if (imp) {
          // ky = y;
          // kx = x;
          // kBig = i == 0;
          // }
          // imp ? IMPORTANT :
          UType important = OBSTACLE;
          Direction dir = rndDir(random, values);
          us[i] = new U(important, dir);
          us[1 - i] = null;
        } else {
          us[0] = null;
          us[1] = null;
        }
        Block block = new Block();
        block.type = t;
        block.smallU = us[0];
        block.bigU = us[1];
        row.cols.add(block);
      }
    }
    for (int i = 0; i < 2; i++) {
      int gy, gx;
      do {
        gy = random.nextInt(h - 2) + 1;
        gx = random.nextInt(w - 2) + 1;
      } while ((gy == py && gy == px) || (gy == ky && gx == kx));
      ky = gy;
      kx = gx;
      Block block = level.rows.get(gy).cols.get(gx);
      if (kBig) {
        if (block.bigU == null) {
          block.bigU = new U(IMPORTANT, rndDir(random, values));
        } else {
          block.bigU.type = IMPORTANT;
        }
        block.smallU = null;
        kBig = false;
      } else {
        if (block.smallU == null) {
          block.smallU = new U(IMPORTANT, rndDir(random, values));
        } else {
          block.smallU.type = IMPORTANT;
        }
        block.bigU = null;
        kBig = true;
      }
    }
    return level;
  }

  public int[] randomPos(Random random, int h, int w) {
    int ey = h - 1;
    int ex = w - 1;
    if (random.nextBoolean()) {
      ey = random.nextInt(h - 2) + 1;
    } else {
      ex = random.nextInt(w - 2) + 1;
    }
    int[] ints = {ey, ex};
    return ints;
  }

  public Level generateV2(int seed) {
    Random random = new Random(seed);
    int h = random.nextInt(3) + 4;
    int w = random.nextInt(3) + 4;

    int py = random.nextInt(h - 2) + 1;
    int px = random.nextInt(w - 2) + 1;

    int[] randomPos = randomPos(random, h, w);
    int ey = randomPos[0];
    int ex = randomPos[1];

    randomPos = randomPos(random, h, w);
    int p1y = randomPos[0];
    int p1x = randomPos[1];
    Direction p1Dir = getPortalDir(p1y, p1x, h, w);

    randomPos = randomPos(random, h, w);
    int p2y = randomPos[0];
    int p2x = randomPos[1];
    Direction p2Dir = getPortalDir(p2y, p2x, h, w);

    int ky = -1;
    int kx = -1;
    boolean kBig = false;

    Direction[] values = Direction.values();
    Level level = new Level("genv2s" + seed, "genv2s" + seed);
    U[] us = new U[2];
    for (int y = 0; y < h; y++) {
      Row row = new Row();
      level.rows.add(row);
      for (int x = 0; x < w; x++) {
        Block block = new Block();
        block.type = DEFAULT;
        if (x == px && y == py) {
          block.type = PLAYER;
        } else if (x == ex && y == ey) {
          block.type = GATE;
        } else if (x == p2x && y == p2y) {
          block.type = PORTAL;
          block.portal = new Portal(p2Dir);
          block.num = 2;
        } else if (x == p1x && y == p1y) {
          block.type = PORTAL;
          block.portal = new Portal(p1Dir);
          block.num = 1;
        } else if (x == 0 || x == w - 1 || y == 0 || y == h - 1) {
          block.type = BOUNDS;
        }

        if (block.type == DEFAULT && random.nextBoolean()) {
          int i = random.nextInt(2);
          // boolean imp = random.nextBoolean();
          // if (imp) {
          // ky = y;
          // kx = x;
          // kBig = i == 0;
          // }
          // imp ? IMPORTANT :
          UType important = OBSTACLE;
          Direction dir = rndDir(random, values);
          us[i] = new U(important, dir);
          us[1 - i] = null;
        } else {
          us[0] = null;
          us[1] = null;
        }
        block.smallU = us[0];
        block.bigU = us[1];
        row.cols.add(block);
      }
    }
    for (int i = 0; i < 2; i++) {
      int gy, gx;
      do {
        gy = random.nextInt(h - 2) + 1;
        gx = random.nextInt(w - 2) + 1;
      } while ((gy == py && gy == px) || (gy == ky && gx == kx));
      ky = gy;
      kx = gx;
      Block block = level.rows.get(gy).cols.get(gx);
      if (kBig) {
        if (block.bigU == null) {
          block.bigU = new U(IMPORTANT, rndDir(random, values));
        } else {
          block.bigU.type = IMPORTANT;
        }
        block.smallU = null;
        kBig = false;
      } else {
        if (block.smallU == null) {
          block.smallU = new U(IMPORTANT, rndDir(random, values));
        } else {
          block.smallU.type = IMPORTANT;
        }
        block.bigU = null;
        kBig = true;
      }
    }
    return level;
  }

  private Direction getPortalDir(int y, int x, int h, int w) {
    return y == 0 ? Direction.BOTTOM : x == 0 ? Direction.RIGHT : x == w - 1 ? Direction.LEFT : Direction.TOP;
  }

  private Direction rndDir(Random random, Direction[] values) {
    return values[random.nextInt(values.length)];
  }

}