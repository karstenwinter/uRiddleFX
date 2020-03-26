package uriddle.logic;

import static uriddle.logic.Direction.*;
import static uriddle.logic.Level.State.*;

import java.io.*;
import java.util.*;

import uriddle.logic.Level.*;

public class Game {
  public static void main(String[] args) throws Exception {
    int index = 0;
    List<String> levels = getLevels();
    Level level = get(levels, index);

    System.out.println("Welcome to uRiddle!");
    System.out.println(
            "Goal: Move the thick (#) boxes into each other to open the exit (X) and leave the level.");
    System.out.println("Controls: Enter w, a, s or d end press Enter to move.");
    System.out.println("Good luck and have fun!");
    System.out.println(level.toString());
    Scanner s = new Scanner(System.in);
    String read;
    while ((read = s.nextLine()) != null) {
      Direction d = getDir(read);
      if (d != null) {
        State go = Logic.instance.go(level, d);
        System.out.println(level.toString());
        if (go == REACHED_EXIT) {
          System.out.println("You reached the exit! Well done!");
          index++;
          level = get(levels, index);
        }
      }
    }
    s.close();
  }

  public static Level get(List<String> levels, int index) {
    if (index >= levels.size() || index < 0) {
      index = 0;
    }
    String entry = levels.get(index);
    Level level = LevelReader.instance.fromString(entry);
    return level;
  }

  static Direction getDir(String read) {
    switch (read.charAt(0)) {
      case 'w':
        return TOP;
      case 's':
        return BOTTOM;
      case 'a':
        return LEFT;
      case 'd':
        return RIGHT;
      default:
        return null;
    }
  }

  public static List<String> getLevels(BufferedReader rd) throws Exception {
    List<String> res = new ArrayList<String>();
    String line;
    String lastName = null;
    String name = null;
    StringBuilder entry = new StringBuilder();
    try {
      while ((line = rd.readLine()) != null) {
        if (line.startsWith(" ")) {
          entry.append(line).append("\n");
        } else {
          if (name != null && name != lastName) {
            if (entry.length() != 0) {
              entry.delete(entry.length() - 1, entry.length());
            }
            entry.insert(0, name + "\n");
            res.add(entry.toString());
            entry.delete(0, entry.length());
            lastName = name;
          }
          name = line;
        }
      }
    } finally {
      if (rd != null) {
        rd.close();
      }
    }
    return res;
  }

  public static List<String> getLevels() throws Exception {
    InputStream in = Game.class.getResourceAsStream("levels.txt");
    BufferedReader rd = new BufferedReader(new InputStreamReader(in));
    return getLevels(rd);
  }
}