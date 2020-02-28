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

  private Direction rndDir(Random random, Direction[] values) {
    return values[random.nextInt(values.length)];
  }
}