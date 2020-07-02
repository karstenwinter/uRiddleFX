package test;

import static uriddle.logic.Block.BlockType.*;
import static uriddle.logic.Direction.*;
import static uriddle.logic.Level.State.*;
import static uriddle.logic.U.UType.*;

import junit.framework.*;
import uriddle.logic.*;
import uriddle.logic.Level.*;

// @formatter:off
public class LogicTest extends TestCase {
  U noBig = null;
  U noSmall = null;

  public void testLogic() {
    Level level = new Level("", "",
            new Row(
                    new Block(PLAYER, new U(OBSTACLE, RIGHT), new U(IMPORTANT, BOTTOM)),
                    new Block(DEFAULT, noBig, noSmall)
            )
    );
    assertEquals(
            ": \n" +
                    "             \n" +
                    " +++++       \n" +
                    " +###        \n" +
                    " +#o#        \n" +
                    " +# #        \n" +
                    " +++++       \n" +
                    "             ",
            level.toString()
    );
    State res = Logic.instance.go(level, RIGHT);
    assertEquals(
            ": \n" +
                    "             \n" +
                    " +++++       \n" +
                    " +      ###  \n" +
                    " +      #o#  \n" +
                    " +      # #  \n" +
                    " +++++       \n" +
                    "             ",
            level.toString()
    );
    assertEquals(MOVED, res);
  }

  public void testLogic2() {
    Level level = new Level("", "",
            new Row(
                    new Block(PLAYER, new U(OBSTACLE, RIGHT), new U(IMPORTANT, BOTTOM)),
                    new Block(DEFAULT, new U(OBSTACLE, LEFT), noSmall)
            )
    );
    assertEquals(
            ": \n" +
                    "             \n" +
                    " +++++ +++++ \n" +
                    " +###      + \n" +
                    " +#o#      + \n" +
                    " +# #      + \n" +
                    " +++++ +++++ \n" +
                    "             ",
            level.toString()
    );
    State res = Logic.instance.go(level, RIGHT);
    assertEquals(
            ": \n" +
                    "             \n" +
                    " +++++ +++++ \n" +
                    " +      ###+ \n" +
                    " +      #o#+ \n" +
                    " +      # #+ \n" +
                    " +++++ +++++ \n" +
                    "             ",
            level.toString()
    );
    assertEquals(MOVED, res);
  }
}
// @formatter:on