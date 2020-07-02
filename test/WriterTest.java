package test;


import static solid.collectors.ToArray.toArray;
import static solid.stream.Stream.stream;
import static uriddle.logic.Block.BlockType.*;
import static uriddle.logic.Direction.*;
import static uriddle.logic.U.UType.*;

import junit.framework.*;
import uriddle.logic.*;


import junit.framework.*;
import uriddle.logic.Block;
import uriddle.logic.Level;
import uriddle.logic.Level.State;
import uriddle.logic.Row;
import uriddle.logic.U;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

import static uriddle.logic.Block.BlockType.DEFAULT;
import static uriddle.logic.Block.BlockType.PLAYER;
import static uriddle.logic.Direction.*;
import static uriddle.logic.Level.State.MOVED;
import static uriddle.logic.U.UType.IMPORTANT;
import static uriddle.logic.U.UType.OBSTACLE;

// @formatter:off
public class WriterTest extends TestCase {
    U noBig = null;
    U noSmall = null;

    public void assertArrayEquals(Object[] a, Object[] b) {
        if (!Arrays.deepEquals(a, b)) {
            fail(Arrays.deepToString(a) + "\n!=\n" + Arrays.deepToString(b));
        }
    }

    public void testFormatting() {
        assertEquals(
                "a: b\n" +
                        "                         \n" +
                        "                         \n" +
                        "  + +   +++   ###   ###  \n" +
                        "  + +   +     # #     #  \n" +
                        "  +++   +++   # #   ###  \n" +
                        "                         \n" +
                        "                         \n" +
                        " #   # ##### +++++ +++++ \n" +
                        " #   # #     +   +     + \n" +
                        " #   # #     +   +     + \n" +
                        " #   # #     +   +     + \n" +
                        " ##### ##### +   + +++++ \n" +
                        "                         ",
                new Level("a", "b",
                        new Row(
                                new Block(DEFAULT, noBig, new U(OBSTACLE, TOP)),
                                new Block(DEFAULT, noBig, new U(OBSTACLE, RIGHT)),
                                new Block(DEFAULT, noBig, new U(IMPORTANT, BOTTOM)),
                                new Block(DEFAULT, noBig, new U(IMPORTANT, LEFT))
                        ),
                        new Row(
                                new Block(DEFAULT, new U(IMPORTANT, TOP), noSmall),
                                new Block(DEFAULT, new U(IMPORTANT, RIGHT), noSmall),
                                new Block(DEFAULT, new U(OBSTACLE, BOTTOM), noSmall),
                                new Block(DEFAULT, new U(OBSTACLE, LEFT), noSmall)
                        )
                ).toString());

        assertEquals(
                ": \n" +
                        "                   \n" +
                        " +++++       ##### \n" +
                        " +###        ####  \n" +
                        " +# #        ##    \n" +
                        " +# #        ####  \n" +
                        " +++++       ##### \n" +
                        "                   ",
                new Level("", "",
                        new Row(
                                new Block(DEFAULT, new U(OBSTACLE, RIGHT), new U(IMPORTANT, BOTTOM)),
                                new Block(DEFAULT, noBig, noSmall),
                                new Block(DEFAULT, new U(IMPORTANT, RIGHT), new U(IMPORTANT, RIGHT))
                        )
                ).toString());

        assertEquals(
                ": \n" +
                        "                   \n" +
                        "                   \n" +
                        "  ###   \\ /    ^   \n" +
                        "  #o#    X    <+>  \n" +
                        "  # #   / \\    v   \n" +
                        "                   \n" +
                        "                   ",
                new Level("", "",
                        new Row(
                                new Block(PLAYER, noBig, new U(IMPORTANT, BOTTOM)),
                                new Block(GATE, noBig, noSmall),
                                new Block(EXIT, noBig, noSmall)
                        )
                ).toString());
    }


    public void testAnimation1() throws Throwable {
        String str = ": \n" +
                "                               \n" +
                " ..... ##### #####       ..... \n" +
                " ..... #+ +      #       ..... \n" +
                " ..... #+o+      #       ..... \n" +
                " ..... #+++      #       ..... \n" +
                " ..... ##### #####       ..... \n" +
                "                               ";
        Level l = LevelReader.instance.fromString(str);
        //	System.out.println(l);
        Map.Entry<State, String[]> stateEntry = Logic.instance.goWithAnimation(l, RIGHT);
        int i = -1;
        for (String s : stateEntry.getValue()) {
            i += 1;
            System.out.println("ANIM " + i);
            System.out.println("<" + s.replace('.', 'W').replace(' ', '.') + ">");
        }
        String[] arr = {
                //str,
                ": \n" +
                        "                               \n" +
                        " ..... ##### #####       ..... \n" +
                        " ..... # + ++    #       ..... \n" +
                        " ..... # +o++    #       ..... \n" +
                        " ..... # ++++    #       ..... \n" +
                        " ..... ##### #####       ..... \n" +
                        "                               ",
                ": \n" +
                        "                               \n" +
                        " ..... ##### #####       ..... \n" +
                        " ..... #  +  +   #       ..... \n" +
                        " ..... #  +oo+   #       ..... \n" +
                        " ..... #  ++++   #       ..... \n" +
                        " ..... ##### #####       ..... \n" +
                        "                               ",
                ": \n" +
                        "                               \n" +
                        " ..... ##### #####       ..... \n" +
                        " ..... #   ++ +  #       ..... \n" +
                        " ..... #   ++o+  #       ..... \n" +
                        " ..... #   ++++  #       ..... \n" +
                        " ..... ##### #####       ..... \n" +
                        "                               ",
                ": \n" +
                        "                               \n" +
                        " ..... ##### #####       ..... \n" +
                        " ..... #     + + #       ..... \n" +
                        " ..... #     +o+ #       ..... \n" +
                        " ..... #     +++ #       ..... \n" +
                        " ..... ##### #####       ..... \n" +
                        "                               ",
                ": \n" +
                        "                               \n" +
                        " ..... ##### #####       ..... \n" +
                        " ..... #      + +#       ..... \n" +
                        " ..... #      +o+#       ..... \n" +
                        " ..... #      +++#       ..... \n" +
                        " ..... ##### #####       ..... \n" +
                        "                               ",
        };
        String[] collect = stream(stateEntry.getValue())
                .map(x -> x.replace('\'', '#')
                        .replace('-', '+'))
                .collect(toArray(String.class));
        assertArrayEquals(arr, collect);
    }

    public void testAnimation2() throws Throwable {
        String str = ": \n" +
                "                               \n" +
                " ..... +++++ #####       ..... \n" +
                " ..... ++ +      #       ..... \n" +
                " ..... ++o+      #       ..... \n" +
                " ..... ++++      #       ..... \n" +
                " ..... +++++ #####       ..... \n" +
                "                               ";
        Level l = LevelReader.instance.fromString(str);
        //	System.out.println(l);
        Map.Entry<State, String[]> stateEntry = Logic.instance.goWithAnimation(l, RIGHT);
        int i = -1;
        for (String s : stateEntry.getValue()) {
            i += 1;
            System.out.println("ANIM " + i);
            System.out.println("<" + s.replace('.', 'W').replace(' ', '.') + ">");
        }
        String[] arr = {
                //str,
                ": \n" +
                        "                               \n" +
                        " ..... +++++ #####       ..... \n" +
                        " ..... + + ++    #       ..... \n" +
                        " ..... + +o++    #       ..... \n" +
                        " ..... + ++++    #       ..... \n" +
                        " ..... +++++ #####       ..... \n" +
                        "                               ",
                ": \n" +
                        "                               \n" +
                        " ..... +++++ #####       ..... \n" +
                        " ..... +  +  +   #       ..... \n" +
                        " ..... +  +oo+   #       ..... \n" +
                        " ..... +  ++++   #       ..... \n" +
                        " ..... +++++ #####       ..... \n" +
                        "                               ",
                ": \n" +
                        "                               \n" +
                        " ..... +++++ #####       ..... \n" +
                        " ..... +   ++ +  #       ..... \n" +
                        " ..... +   ++o+  #       ..... \n" +
                        " ..... +   ++++  #       ..... \n" +
                        " ..... +++++ #####       ..... \n" +
                        "                               ",
                ": \n" +
                        "                               \n" +
                        " ..... +++++ #####       ..... \n" +
                        " ..... +     + + #       ..... \n" +
                        " ..... +     +o+ #       ..... \n" +
                        " ..... +     +++ #       ..... \n" +
                        " ..... +++++ #####       ..... \n" +
                        "                               ",
                ": \n" +
                        "                               \n" +
                        " ..... +++++ #####       ..... \n" +
                        " ..... +      + +#       ..... \n" +
                        " ..... +      +o+#       ..... \n" +
                        " ..... +      +++#       ..... \n" +
                        " ..... +++++ #####       ..... \n" +
                        "                               ",
        };
        String[] collect = stream(stateEntry.getValue())
                .map(x -> x.replace('\'', '#')
                        .replace('-', '+'))
                .collect(toArray(String.class));
        assertArrayEquals(arr, collect);
    }
}
// @formatter:on