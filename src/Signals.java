import Exceptions.HDLDuplicateSignalException;
import Exceptions.HDLException;
import Exceptions.HDLParseException;

import java.util.*;

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
    // Stores signals that are declared. Signals get removed when a driving expression is added.
    private HashSet<String> noExpressionYet;

    /**
     * A has B in its list if A is a wire and depends on B.
     * Has a special TERMINATION key which maps to the signals used in the
     * termination expression.
     */
    private HashMap<String, HashSet<String>> dependencies;

    /**
     * Boolean that represents which phase we are in.
     * TRUE if all signals have been added, and we can now begin to execute.
     * FALSE if we are still building the object.
     */
    private boolean built;

    private ArrayList<String> wireOrder;

    /**
     * Creates a new Signals object, initializing all relevant internal data structures
     */
    public Signals() {
        this.values = new HashMap<>();
        this.expressions = new HashMap<>();
        this.regs = new HashSet<>();
        this.wires = new HashSet<>();
        this.noExpressionYet = new HashSet<>();
        this.dependencies = new HashMap<>();
        this.wireOrder = null;
    }



    // METHODS TO BE CALLED BY HDL_FILE_READER //////////////////////////////////

    /**
     * Adds a new reg to the Signals object, and sets its starting value.
     *
     * Throws HDLDuplicateSignalException if SIGNAL has already been declared.
     */
    public void addReg(String signal, int initVal) throws HDLDuplicateSignalException {
        if (regs.contains(signal) || wires.contains(signal)) {
            String msg = String.format("%s has already been declared in this HDL file!", signal);
            throw new HDLDuplicateSignalException(msg);
        }

        regs.add(signal);
        values.put(signal, initVal);
        noExpressionYet.add(signal);
    }

    /**
     * Adds a new wire to the Signals object
     *
     * Throws HDLDuplicateSignalException if SIGNAL has already been declared.
     */
    public void addWire(String signal) throws HDLDuplicateSignalException {
        if (regs.contains(signal) || wires.contains(signal)) {
            String msg = String.format("%s has already been declared in this HDL file!", signal);
            throw new HDLDuplicateSignalException(msg);
        }

        wires.add(signal);
        values.put(signal, -1);
        noExpressionYet.add(signal);
    }

    /**
     * Adds an expression for a signal that is being tracked. SIGNAL must match
     * the name of a signal that was previously added via addReg or addWire.
     *
     * Throws HDLDuplicateSignalException if SIGNAL already has a driving expression.
     * Throws HDLParseException if SIGNAL has not been declared yet or one of
     * the signals it depends on has not been declared yet.
     */
    public void addExpression(String signal, String expressionStr) throws HDLDuplicateSignalException, HDLParseException {
        if (expressions.containsKey(signal)) {
            String msg = String.format("%s already has a driving expression!", signal);
            throw new HDLDuplicateSignalException(msg);
        }
        if (!wires.contains(signal) && !regs.contains(signal)) {
            String msg = String.format("%s has not been declared yet!", signal);
            throw new HDLParseException(msg);
        }

        Expression expression = new Expression(expressionStr);
        expressions.put(signal, expression);
        noExpressionYet.remove(signal);

        // Find out dependencies and make sure they have been declared too
        HashSet<String> dependenciesSet = expression.getSignalNames();
        for (String d : dependenciesSet) {
            if (!wires.contains(d) && !regs.contains(d)) {
                String msg = String.format("The expression for %s, <<  %s  >>, has a dependency on %s, which has not been declared.",
                        signal, expressionStr, d);
                throw new HDLParseException(msg);
            }
        }

        // If it is a wire then add the dependencies
        if (wires.contains(signal)) {
            dependencies.put(signal, dependenciesSet);
        }
    }

    /**
     * Adds a termination condition to the Signals object. The program execution
     * will be stopped as soon as this expression evaluates to any value other than 0.
     *
     * Throws HDLDuplicateSignalException if a TERMINATE expression has already been declared.
     * Throws HDLParseException if one of the signals it depends on has not been declared yet.
     */
    public void addTerminate(String expressionStr) throws HDLDuplicateSignalException, HDLParseException {
        addWire("TERMINATE");
        addExpression("TERMINATE", expressionStr);
    }

    /**
     * Final step in building this object before it is able to be used for execution.
     * Reads dependency lists to create topological sort order for wire evaluation.
     *
     * Throws HDLParseException if a signal has no expression
     * Throws HDLException if a cycle is found in the wire dependency graph.
     */
    public void build() throws HDLException {
        // Use DFS Topological Sort Algorithm

        checkForExpressions();

        ArrayList<String> topologicalSort = new ArrayList<>();

        HashMap<String, Integer> inDegree = new HashMap<>();
        for (String wire : wires) { inDegree.put(wire, 0); }
        for (String u : wires) {
            for (String v : dependencies.get(u)) {
                if (wires.contains(v)) {
                    inDegree.put(v, inDegree.get(v) + 1);
                }
            }
        }

        int notVisited = wires.size();

        Stack<String> inDegree0 = new Stack<>();
        for (String wire : wires) {
            if (inDegree.get(wire) == 0) { inDegree0.add(wire); }
        }

        while (inDegree0.size() > 0) {
            String u = inDegree0.pop();
            topologicalSort.add(0, u);
            notVisited--;
            for (String v : dependencies.get(u)) {
                if (wires.contains(v)) {
                    inDegree.put(v, inDegree.get(v) - 1);
                    if (inDegree.get(v) == 0) {
                        inDegree0.add(v);
                    }
                }
            }
        }

        if (notVisited > 0) {
            throw new HDLException("Cycle detected in wire dependencies!");
        }

        this.wireOrder = topologicalSort;

        // Get first values of wires
        for (String wire : wireOrder) {
            int val = expressions.get(wire).eval(values);
            values.put(wire, val);
        }

        this.built = true;
    }

    private void checkForExpressions() throws HDLParseException {
        if (!noExpressionYet.isEmpty()) {
            String msg = "The following signals have no driving expression: " + noExpressionYet;
            throw new HDLParseException(msg);
        }
    }

    public void step() {
        assert built : "Must call build() before stepping!";

        HashMap<String, Integer> nextValues = new HashMap<>();
        for (String reg : regs) {
            int nextVal = expressions.get(reg).eval(values);
            nextValues.put(reg, nextVal);
        }
        for (String wire : wireOrder) {
            int val = expressions.get(wire).eval(nextValues);
            nextValues.put(wire, val);
        }
        values = nextValues;
    }

    public int stepToTerminate() {
        while (values.get("TERMINATE") == 0) {
            step();
        }
        return values.get("TERMINATE");
    }

    // GETTERS ////////////////////////////////////////////////////////////////

    public HashMap<String, Integer> getValues() {
        return values;
    }

    public HashMap<String, Expression> getExpressions() {
        return expressions;
    }

    public HashSet<String> getRegs() {
        return regs;
    }

    public HashSet<String> getWires() {
        return wires;
    }

    public HashSet<String> getNoExpressionYet() {
        return noExpressionYet;
    }

    public HashMap<String, HashSet<String>> getDependencies() {
        return dependencies;
    }

    public boolean isBuilt() {
        return built;
    }

    public ArrayList<String> getWireOrder() {
        return wireOrder;
    }

}
