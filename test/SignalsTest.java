import Exceptions.HDLDuplicateSignalException;
import Exceptions.HDLException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Jonah Tharakan
 *
 * Unit/Integration tests for Signals class
 */
class SignalsTest {

    /**
     * Creates a working Signals object
     */
    private Signals buildSignals1() throws HDLException {
        Signals signals = new Signals();
        signals.addReg("A", 1); // reg A = 0;
        signals.addExpression("A", "(A + 1)"); // A <= (A + 1);
        signals.addTerminate("(A == 10)"); // TERMINATE (A == 10);
        return signals;
    }

    // Test building of object ////////////////////////////////////////////////

    @Test
    public void simpleBuildTest() {
        try {
            Signals signals = buildSignals1();

            assertEquals(1, signals.getRegs().size());
            assertTrue(signals.getRegs().contains("A"));
            assertEquals(1, signals.getWires().size());
            assertTrue(signals.getWires().contains("TERMINATE"));

            assertEquals(2, signals.getExpressions().size());
            assertEquals("(A + 1)", signals.getExpressions().get("A").toString());
            assertEquals("(A == 10)", signals.getExpressions().get("TERMINATE").toString());

            assertEquals(1, signals.getDependencies().size());
            assertEquals(1, signals.getDependencies().get("TERMINATE").size());
            assertTrue(signals.getDependencies().get("TERMINATE").contains("A"));

            assertEquals(2, signals.getValues().size());
            assertEquals(1, signals.getValues().get("A"));

            assertEquals(0, signals.getNoExpressionYet().size());

            assertFalse(signals.isBuilt());
        }
        catch (HDLException e) {
            e.printStackTrace();
            fail("HDLException was thrown");
        }
    }


    // Test running of object /////////////////////////////////////////////////

}