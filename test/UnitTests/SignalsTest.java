package UnitTests;

import Source.Signals;
import Exceptions.HDLDuplicateSignalException;
import Exceptions.HDLException;
import Exceptions.HDLParseException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Jonah Tharakan
 *
 * Unit tests for HDLSim.Signals class
 */
class SignalsTest {

    /**
     * Creates a working HDLSim.Signals object
     */
    private Signals makeSignals1() throws HDLException {
        Signals signals = new Signals();
        signals.addReg("A", 1); // reg A = 0;
        signals.addExpression("A", "(A + 1)"); // A <= (A + 1);
        signals.addTerminate("(A == 10)"); // TERMINATE (A == 10);
        return signals;
    }

    /**
     * Creates a working HDLSim.Signals object with a more complex wire structure
     */
    private Signals makeSignals2() throws HDLException {
        Signals signals = new Signals();
        signals.addReg("store", 1);
        signals.addWire("A");
        signals.addWire("B");
        signals.addWire("C");
        signals.addWire("D");
        signals.addWire("E");
        signals.addExpression("store", "(A + E)");
        signals.addExpression("A", "(B + C)");
        signals.addExpression("B", "!E");
        signals.addExpression("C", "(B + D)");
        signals.addExpression("D", "(store | 1)");
        signals.addExpression("E", "(D | 1)");
        signals.addTerminate("(A == C)");
        return signals;
    }

    // Test building of object ////////////////////////////////////////////////

    @Test
    public void simpleNoBuildTest() {
        try {
            Signals signals = makeSignals1();

            Assertions.assertEquals(1, signals.getRegs().size());
            Assertions.assertTrue(signals.getRegs().contains("A"));
            Assertions.assertEquals(1, signals.getWires().size());
            Assertions.assertTrue(signals.getWires().contains("TERMINATE"));

            Assertions.assertEquals(2, signals.getExpressions().size());
            Assertions.assertEquals("(A + 1)", signals.getExpressions().get("A").toString());
            Assertions.assertEquals("(A == 10)", signals.getExpressions().get("TERMINATE").toString());

            Assertions.assertEquals(1, signals.getDependencies().size());
            Assertions.assertEquals(1, signals.getDependencies().get("TERMINATE").size());
            Assertions.assertTrue(signals.getDependencies().get("TERMINATE").contains("A"));

            Assertions.assertEquals(2, signals.getValues().size());
            Assertions.assertEquals(1, signals.getValues().get("A"));

            Assertions.assertEquals(0, signals.getNoExpressionYet().size());

            Assertions.assertFalse(signals.isBuilt());
        }
        catch (HDLException e) {
            e.printStackTrace();
            fail("HDLException was thrown");
        }
    }

    @Test
    public void simpleBuildTest() {
       try {
           Signals signals = makeSignals1();
           Assertions.assertFalse(signals.isBuilt());
           signals.build();
           Assertions.assertTrue(signals.isBuilt());

           ArrayList<String> ord = signals.getWireOrder();
           assertEquals(1, ord.size());
           assertEquals("TERMINATE", ord.get(0));
       }
       catch (HDLException e) {
           e.printStackTrace();
           fail("An HDLException was thrown");
       }
    }

    @Test // exception test
    public void repeatDeclarationTest() {
        try {
            Signals signals = new Signals();
            signals.addReg("A", 10);
            signals.addWire("B");
            signals.addReg("A", 11);
            signals.addExpression("A", "(B + 1)");
            signals.addExpression("B", "~A");
            signals.addTerminate("(A == 20)");
            signals.build();

            fail("No exception was thrown yet one was expected.");
        } catch (HDLDuplicateSignalException e) {
            assertEquals("A has already been declared in this HDL file!", e.getMessage());
        } catch (Exception e) {
            fail("Wrong exception was thrown.");
        }
    }

    @Test // exception test
    public void repeatExpressionTest() {
        try {
            Signals signals = new Signals();
            signals.addReg("A", 10);
            signals.addWire("B");
            signals.addExpression("A", "(B + 1)");
            signals.addExpression("B", "~A");
            signals.addExpression("B", "!A");
            signals.addTerminate("(A == 20)");
            signals.build();

            fail("No exception was thrown yet one was expected.");
        } catch (HDLDuplicateSignalException e) {
            assertEquals("B already has a driving expression!", e.getMessage());
        } catch (Exception e) {
            fail("Wrong exception was thrown.");
        }
    }

    @Test // exception test
    public void usingUndeclaredSignalTest1() {
        try {
            Signals signals = new Signals();
            signals.addReg("A", 10);
            signals.addWire("B");
            signals.addExpression("A", "(B + 1)");
            signals.addExpression("B", "~C");
            signals.addTerminate("(A == 20)");
            signals.build();

            fail("No exception was thrown yet one was expected.");
        } catch (HDLParseException e) {
            assertEquals("The expression for B, <<  ~C  >>, has a dependency on C, which has not been declared.", e.getMessage());
        } catch (Exception e) {
            fail("Wrong exception was thrown.");
        }
    }

    @Test // exception test
    public void usingUndeclaredSignalTest2() {
        try {
            Signals signals = new Signals();
            signals.addReg("A", 10);
            signals.addWire("B");
            signals.addExpression("A", "(B + 1)");
            signals.addExpression("C", "~A");
            signals.addTerminate("(A == 20)");
            signals.build();

            fail("No exception was thrown yet one was expected.");
        } catch (HDLParseException e) {
            assertEquals("C has not been declared yet!", e.getMessage());
        } catch (Exception e) {
            fail("Wrong exception was thrown.");
        }
    }

    @Test // exception test
    public void noExpressionBeforeBuildTest() {
        try {
            Signals signals = new Signals();
            signals.addReg("A", 10);
            signals.addWire("B");
            signals.addExpression("A", "(B + 1)");
            signals.addTerminate("(A == 20)");
            signals.build();

            fail("No exception was thrown yet one was expected.");
        } catch (HDLParseException e) {
            assertEquals("The following signals have no driving expression: [B]", e.getMessage());
        } catch (Exception e) {
            fail("Wrong exception was thrown.");
        }
    }


    @Test
    public void dagBuildTest() {
        try {
            Signals signals = makeSignals2();
            signals.build();

            ArrayList<String> ord = signals.getWireOrder();
            assertEquals(6, ord.size());
            ArrayList<String> expected = new ArrayList<>(Arrays.asList(
                    "D", "E", "B", "C", "A", "TERMINATE"
            ));
            assertEquals(expected, ord);
        }
        catch (HDLException e) {
            e.printStackTrace();
            fail("An HDLException was thrown");
        }
    }

    @Test // exception test
    public void cycleDetectionTest() {
        try {
            Signals signals = new Signals();
            signals.addReg("store", 1);
            signals.addWire("A");
            signals.addWire("B");
            signals.addWire("C");
            signals.addWire("D");
            signals.addExpression("store", "(A + (B + (C + D)))");
            // A depends on B depends on C depends on A
            signals.addExpression("A", "(B + 1)");
            signals.addExpression("B", "(C + 2)");
            signals.addExpression("C", "(A & 3)");
            signals.addExpression("D", "~A");
            signals.addTerminate("(D == 5)");
            signals.build();

            fail("No exception was thrown, but one was expected.");
        } catch (HDLException e) {
            assertEquals("Cycle detected in wire dependencies!", e.getMessage());
        } catch (Exception e) {
            fail("Wrong exception was thrown.");
        }
    }


    // Test running of object /////////////////////////////////////////////////

}