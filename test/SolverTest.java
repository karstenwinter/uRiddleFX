package test;

import junit.framework.TestCase;
import uriddle.logic.*;

// @formatter:off
public class SolverTest extends TestCase {
    U noBig = null;
    U noSmall = null;

    public void testSolver() {

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

        String res2 = "";
        int minSteps = 10;
        for (int i = 3600; i < 12000; i++) {
            Level level = new Generator().generate(i);
            String s = level.toString();
            Solver solver = new Solver();
            Res res = solver.solve(level);
            if (res.solved && res.iterations > minSteps) {
                // System.out.println("Solved gen" + i + " after " + res.iterations + " steps.");
                System.out.println(s);
                res2 += level.name + "~ai~" + res.iterations + ";";
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
