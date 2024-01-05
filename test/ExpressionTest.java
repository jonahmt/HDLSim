import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

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
}