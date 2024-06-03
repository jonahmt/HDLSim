import Exceptions.HDLDuplicateSignalException;
import Exceptions.HDLException;
import Exceptions.HDLParseException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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


    /**
     * The topological order. Will be null until build() is called.
     * Only includes wires. Used for updating signals.
     */
    private ArrayList<String> wireOrder;
    /**
     * The alphabetical order. Will be null until build() is called.
     * Includes wires and regs. Used for writing output.
     */
    private ArrayList<String> lexicographicalOrder;

    // Output directory where .../result.txt and .../log.txt will be added
    private File outputDir;
    // Fast file writer, used to write to .../log.txt
    private BufferedWriter logWriter;

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
        this.lexicographicalOrder = null;

        this.outputDir = null;
    }

    /**
     * Sets the output directory to the specified path.
     * Creates a directory at that location if one does not already exist.
     * Exits program with status code 1 if a problem occurs.
     */
    public void setOutputDir(String outputDirPath) {
        try {
            outputDir = new File(outputDirPath);
            outputDir.mkdirs();

            if (!HDLSim.checkFlag("no-log")) {
                FileWriter fw = new FileWriter(outputDir.getPath() + "/log.txt");
                this.logWriter = new BufferedWriter(fw);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            System.out.println("\nFatal IO exception occurred - Exiting program");
            System.exit(1);
        }
    }


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
        checkForExpressions();
        buildLexicographicOrder();

        // Use DFS Topological Sort Algorithm
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

        try {
            dumpCurrentValues(this.logWriter);
        }
        catch (IOException e) {
            e.printStackTrace();
            System.out.println("\nFatal IO exception occurred - Exiting program");
            System.exit(1);
        }

        this.built = true;
    }

    /**
     * Generates an alphabetical ordering of the signals.
     */
    private void buildLexicographicOrder() {
        assert this.built;
        lexicographicalOrder = new ArrayList<>(values.keySet());
        lexicographicalOrder.sort(String::compareTo);
        lexicographicalOrder.remove("TERMINATE");
        lexicographicalOrder.add(0, "TERMINATE");
    }


    /**
     * Makes sure there are no "hanging signals" which have no driving expression.
     * If one is found, throws an exception.
     */
    private void checkForExpressions() throws HDLParseException {
        if (!noExpressionYet.isEmpty()) {
            String msg = "The following signals have no driving expression: " + noExpressionYet;
            throw new HDLParseException(msg);
        }
    }

    /**
     * Executes a single clock cycle of the HDL. Adds the signal values to the log.
     */
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

        if (!HDLSim.checkFlag("no-log")) {
            try {
                dumpCurrentValues(this.logWriter);
            }
            catch (IOException e) {
                e.printStackTrace();
                System.out.println("\nFatal IO exception occurred - Exiting program");
                System.exit(1);
            }
        }
    }

    /**
     * Executes as many clock cycles as necessary until the TERMINATE signal
     * takes on a value other than 0.
     * Returns the final value of the TERMINATE signal.
     */
    public int stepToTerminate() {
        while (values.get("TERMINATE") == 0) {
            step();
        }
        return values.get("TERMINATE");
    }

    /**
     * Writes the current values of all signals to the log in alphabetical order.
     */
    private void dumpCurrentValues(BufferedWriter bw) throws IOException {
        for (String signal : lexicographicalOrder) {
            bw.write(signal + " ");
            if (HDLSim.checkFlag("hex")) {
                bw.write("0x" + Integer.toHexString(values.get(signal)).toUpperCase());
            } else {
                bw.write(Integer.toString(values.get(signal)));
            }
            bw.write('\n');
        }
        bw.write('\n');
    }

    /**
     * Writes the final values of all signals to the log in alphabetical order.
     */
    public void dumpFinalOutput() {
        File out = new File(outputDir.getPath() + "/result.txt");
        try {
            FileWriter fw = new FileWriter(out);
            BufferedWriter bw = new BufferedWriter(fw);

            dumpCurrentValues(bw);

            bw.close();
            fw.close();
        }
        catch (IOException e) {
            e.printStackTrace();
            System.out.println("\nFatal IO exception occurred - Exiting program");
            System.exit(1);
        }
    }

    /**
     * Closes the log writer and flushes it's output.
     */
    public void cleanUp() {
        try {
            this.logWriter.close();
        }
        catch (IOException e) {
            e.printStackTrace();
            System.out.println("\nFatal IO exception occurred - Exiting program");
            System.exit(1);
        }
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
