import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Jonah Tharakan
 *
 * Unit tests for Expression class.
 */

class ExpressionTest {

    @Test
    public void basicConstantTest() {
        assertEquals(0, (new Expression("0")).eval());
        assertEquals(1, (new Expression("1")).eval());
        assertEquals(1000, (new Expression("1000")).eval());
        assertEquals(-777, (new Expression("-777")).eval());
    }

    @Test
    public void basicVariableTest() {
        HashMap<String, Integer> bindings = new HashMap<>();
        bindings.put("A", 0);
        bindings.put("B", 12);
        bindings.put("hello", 345);
        bindings.put("l33tc0d3", 6789);

        assertEquals(0, (new Expression("A").eval(bindings)));
        assertEquals(12, (new Expression("B").eval(bindings)));
        assertEquals(345, (new Expression("hello").eval(bindings)));
        assertEquals(6789, (new Expression("l33tc0d3").eval(bindings)));
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

        assertEquals(1, exp1.eval(bindings));
        assertEquals(0, exp2.eval(bindings));
        assertEquals(0, exp3.eval(bindings));
        assertEquals(1, exp4.eval(bindings));
        assertEquals(1, exp5.eval(bindings));
        assertEquals(0, exp6.eval(bindings));
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

        assertEquals(0xFFFF_FFFF, exp1.eval(bindings));
        assertEquals(0xFFFF_FFFE, exp2.eval(bindings));
        assertEquals(0xFFFF_FFF0, exp3.eval(bindings));
        assertEquals(69, exp4.eval(bindings));
        assertEquals(0xFFFF_FFFF, exp5.eval(bindings));
        assertEquals(0x0000_FFFF, exp6.eval(bindings));
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

        assertEquals(2, exp1.eval(bindings));
        assertEquals(10, exp2.eval(bindings));
        assertEquals(10, exp3.eval(bindings));
        assertEquals(107, exp4.eval(bindings));
        assertEquals(11, exp5.eval(bindings));
        assertEquals(13, exp6.eval(bindings));
        assertEquals(4, exp7.eval(bindings));
    }

    @Test
    public void subtractionTest() {
        Expression exp1 = new Expression("(10 - 8)");
        Expression exp2 = new Expression("(3 - 100)");

        assertEquals(2, exp1.eval());
        assertEquals(-97, exp2.eval());
    }

    @Test
    public void hexBinaryConstantsTest() {
        Expression exp1 = new Expression("0xFF");
        Expression exp2 = new Expression("0b0011");

        assertEquals(255, exp1.eval());
        assertEquals(3, exp2.eval());
    }

    @Test
    public void bitwiseAndOrXorTest() {
        Expression exp1 = new Expression("(0b1100 & 0b1010)");
        Expression exp2 = new Expression("(0b1100 | 0b1010)");
        Expression exp3 = new Expression("(0b1100 ^ 0b1010)");

        assertEquals(0b1000, exp1.eval());
        assertEquals(0b1110, exp2.eval());
        assertEquals(0b0110, exp3.eval());
    }

    @Test
    public void equalityInequalityTest() {
        Expression exp1 = new Expression("(1 == 1)");
        Expression exp2 = new Expression("(1 == 2)");
        Expression exp3 = new Expression("(1 != 1)");
        Expression exp4 = new Expression("(1 != 2)");

        assertEquals(1, exp1.eval());
        assertEquals(0, exp2.eval());
        assertEquals(0, exp3.eval());
        assertEquals(1, exp4.eval());
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

}