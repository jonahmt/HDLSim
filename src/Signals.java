import java.util.HashMap;
import java.util.HashSet;

/**
 * @author Jonah Tharakan
 *
 * Class that represents a set of signals, their current values, and
 * their driving/updating expressions. Created by a call to a FileReader
 * object that was previously constructed with a valid HDL txt file.
 */

public class Signals {

    // Maps signal name to current value
    private HashMap<String, Integer> values;
    // Maps signal name to driving expression
    private HashMap<String, Expression> expressions;

    // For convenience
    private HashSet<String> regs;
    private HashSet<String> wires;

    /**
     * A has B in its list if A is a wire and depends on B.
     * Has a special TERMINATION key which maps to the signals used in the
     * termination expression.
     */
    private HashMap<String, HashSet<String>> dependencies;

    // Expression upon which is true should end the program
    private Expression terminationExpression;

    /**
     * Boolean that represents which phase we are in.
     * TRUE if all signals have been added, and we can now begin to execute.
     * FALSE if we are still building the object.
     */
    private boolean built;

    /**
     * Creates a new Signals object, initializing all relevant internal data structures
     */
    public Signals() {
        this.values = new HashMap<>();
        this.expressions = new HashMap<>();
        this.regs = new HashSet<>();
        this.wires = new HashSet<>();
        this.dependencies = new HashMap<>();
        this.terminationExpression = null;
    }

    // METHODS TO BE CALLED BY HDL_FILE_READER //////////////////////////////////

    /**
     * Adds a new reg to the Signals object, and sets its starting value.
     */
    public void addReg(String signal, int initVal) {

    }

    /**
     * Adds a new wire to the Signals object
     */
    public void addWire(String signal) {

    }

    /**
     * Adds an expression for a signal that is being tracked. SIGNAL must match
     * the name of a signal that was previously added via addReg or addWire.
     */
    public void addExpression(String signal, String expression) {

    }

    /**
     * Adds a termination condition to the Signals object. The program execution
     * will be stopped as soon as this expression evaluates to any value other than 0.
     */
    public void addTerminate(String expression) {

    }

}
