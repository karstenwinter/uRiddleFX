package uriddle.logic;

import uriddle.logic.Solver;
import uriddle.logic.Generator;
import uriddle.logic.Level;
import uriddle.logic.U;

public class SolverMain {

  public static void main(String[] args) {

    //U noBig = null;
    //U noSmall = null;
    // 2 5* 14 29 39*
    // 51* 56 58* 64* 80
        /*
        for (int i : Arrays.asList(2, 14, 29, 56, 80)) {
            Level level = new Generator().generate(i);
            Res res = solver.solve(level);
            Assert.assertEquals(true, res.solved);
            System.out.println("Solved after " + res.iterations + " steps.");
        }

        for (int i : Arrays.asList(5, 39, 51, 58, 64)) {
            Level level = new Generator().generate(i);
            Res res = solver.solve(level);
            Assert.assertEquals(false, res.solved);
        }*/
    Debug.debug = false;
    String res2 = "";
    int minSteps = 10;
    for (int i = 500; i < 120000; i++) {
      Level level = new Generator().generateV2(i);
      Solver solver = new Solver();
      Res res = solver.solve(level);
      if (res.solved && res.iterations > minSteps) {
        // System.out.println("Solved gen" + i + " after " + res.iterations + " steps.");
        level.name += " (AI " + res.iterations + "" + (res.usedPortal ? ", P" : "") + (res.usedSwitch ? ", S" : "") + ")";
        if (res.usedPortal) {
          System.out.println(level.toString());
          res2 += level.name + "~ai~" + res.iterations + ";";
        }
      }
    }
    System.out.println(res2);

    //} catch (Exception e) {
    //  e.printStackTrace();
    //}
    //System.out.println(level.toString());
  }
}
// @formatter:on
