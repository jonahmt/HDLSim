package UnitTests;

import Source.Expression;
import Exceptions.HDLParseException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


import java.util.HashMap;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Jonah Tharakan
 *
 * Unit tests for HDLSim.Expression class.
 */

class ExpressionTest {

    @Test
    public void basicConstantTest() {
        Assertions.assertEquals(0, (new Expression("0")).eval());
        Assertions.assertEquals(1, (new Expression("1")).eval());
        Assertions.assertEquals(1000, (new Expression("1000")).eval());
        Assertions.assertEquals(-777, (new Expression("-777")).eval());
    }

    @Test
    public void basicVariableTest() {
        HashMap<String, Integer> bindings = new HashMap<>();
        bindings.put("A", 0);
        bindings.put("B", 12);
        bindings.put("hello", 345);
        bindings.put("l33tc0d3", 6789);

        Assertions.assertEquals(0, (new Expression("A").eval(bindings)));
        Assertions.assertEquals(12, (new Expression("B").eval(bindings)));
        Assertions.assertEquals(345, (new Expression("hello").eval(bindings)));
        Assertions.assertEquals(6789, (new Expression("l33tc0d3").eval(bindings)));
    }

    @Test
    public void basicLogicalNotTest() {
        HashMap<String, Integer> bindings = new HashMap<>();
        bindings.put("A", 0);
        bindings.put("B", 0xFFFF_0000);

        Expression exp1 = new Expression("!0");
        Expression exp2 = new Expression("!1");
        Expression exp3 = new Expression("!!0");
        Expression exp4 = new Expression("!!!!!!!!!0"); // 9 !
        Expression exp5 = new Expression("!A");
        Expression exp6 = new Expression("!B");

        Assertions.assertEquals(1, exp1.eval(bindings));
        Assertions.assertEquals(0, exp2.eval(bindings));
        Assertions.assertEquals(0, exp3.eval(bindings));
        Assertions.assertEquals(1, exp4.eval(bindings));
        Assertions.assertEquals(1, exp5.eval(bindings));
        Assertions.assertEquals(0, exp6.eval(bindings));
    }

    @Test
    public void basicBitwiseNotTest() {
        HashMap<String, Integer> bindings = new HashMap<>();
        bindings.put("A", 0);
        bindings.put("B", 0xFFFF_0000);

        Expression exp1 = new Expression("~0");
        Expression exp2 = new Expression("~1");
        Expression exp3 = new Expression("~15");
        Expression exp4 = new Expression("~~69");
        Expression exp5 = new Expression("~A");
        Expression exp6 = new Expression("~B");

        Assertions.assertEquals(0xFFFF_FFFF, exp1.eval(bindings));
        Assertions.assertEquals(0xFFFF_FFFE, exp2.eval(bindings));
        Assertions.assertEquals(0xFFFF_FFF0, exp3.eval(bindings));
        Assertions.assertEquals(69, exp4.eval(bindings));
        Assertions.assertEquals(0xFFFF_FFFF, exp5.eval(bindings));
        Assertions.assertEquals(0x0000_FFFF, exp6.eval(bindings));
    }

    @Test
    public void basicAdditionTest() {
        HashMap<String, Integer> bindings = new HashMap<>();
        bindings.put("A", 10);
        bindings.put("B", 3);

        Expression exp1 = new Expression("(1 + 1)");
        Expression exp2 = new Expression("((3 + 4) + (1 + 2))");
        Expression exp3 = new Expression("(  (3   +  4)+  (1+2)         )");
        Expression exp4 = new Expression("(1 + (1 + (1 + (1 + (1 + (1 + (1 + 100)))))))");
        Expression exp5 = new Expression("(A + 1)");
        Expression exp6 = new Expression("(A + B)");
        Expression exp7 = new Expression("(!A + ~~(B + 1))");

        Assertions.assertEquals(2, exp1.eval(bindings));
        Assertions.assertEquals(10, exp2.eval(bindings));
        Assertions.assertEquals(10, exp3.eval(bindings));
        Assertions.assertEquals(107, exp4.eval(bindings));
        Assertions.assertEquals(11, exp5.eval(bindings));
        Assertions.assertEquals(13, exp6.eval(bindings));
        Assertions.assertEquals(4, exp7.eval(bindings));
    }

    @Test
    public void subtractionTest() {
        Expression exp1 = new Expression("(10 - 8)");
        Expression exp2 = new Expression("(3 - 100)");

        Assertions.assertEquals(2, exp1.eval());
        Assertions.assertEquals(-97, exp2.eval());
    }

    @Test
    public void hexBinaryConstantsTest() {
        Expression exp1 = new Expression("0xFF");
        Expression exp2 = new Expression("0b0011");

        Assertions.assertEquals(255, exp1.eval());
        Assertions.assertEquals(3, exp2.eval());
    }

    @Test
    public void bitwiseAndOrXorTest() {
        Expression exp1 = new Expression("(0b1100 & 0b1010)");
        Expression exp2 = new Expression("(0b1100 | 0b1010)");
        Expression exp3 = new Expression("(0b1100 ^ 0b1010)");

        Assertions.assertEquals(0b1000, exp1.eval());
        Assertions.assertEquals(0b1110, exp2.eval());
        Assertions.assertEquals(0b0110, exp3.eval());
    }

    @Test
    public void equalityInequalityTest() {
        Expression exp1 = new Expression("(1 == 1)");
        Expression exp2 = new Expression("(1 == 2)");
        Expression exp3 = new Expression("(1 != 1)");
        Expression exp4 = new Expression("(1 != 2)");

        Assertions.assertEquals(1, exp1.eval());
        Assertions.assertEquals(0, exp2.eval());
        Assertions.assertEquals(0, exp3.eval());
        Assertions.assertEquals(1, exp4.eval());
    }

    @Test
    public void signalNamesTest() {
        Expression exp1 = new Expression("(1 + 1)");
        Expression exp2 = new Expression("(A +((B    ==C) +4))");
        Expression exp3 = new Expression("(ab3523 + (x0q359 - 0xabcdef))");

        HashSet<String> signals;

        signals = exp1.getSignalNames();
        assertEquals(0, signals.size());

        signals = exp2.getSignalNames();
        assertEquals(3, signals.size());
        assertTrue(signals.contains("A"));
        assertTrue(signals.contains("B"));
        assertTrue(signals.contains("C"));

        signals = exp3.getSignalNames();
        assertEquals(2, signals.size());
        assertTrue(signals.contains("ab3523"));
        assertTrue(signals.contains("x0q359"));
    }

    @Test // exception test
    public void noOperatorTest() {
        try {
            Expression exp = new Expression("((1 + 2) (3 + 4))");
            exp.eval();

            fail("Expected exception but none was thrown!");
        } catch (HDLParseException e) {
            assertEquals("Either no or an invalid operator was provided!", e.getMessage());
        } catch (Exception e) {
            fail("Wrong exception was thrown!");
        }
    }

}