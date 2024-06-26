package Source;

import Exceptions.HDLException;
import Exceptions.HDLParseException;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Scanner;

/**
 * @author Jonah Tharakan
 *
 * Class that takes in the path to a valid HDL txt file and reads it to
 * construct a HDLSim.Signals object that encapsulates the logic in the file.
 */

public class HDLModuleReader {

    // The directory that the source file is contained in
    private String dir;

    // File of the source code
    private File file;
    private Scanner sc;
    // Prefix that will be used to name this signal. / for main, parent/instance_name else.
    private String prefix;
    // HDLSim.Signals object to write wires and regs to
    private Signals signals;

    // Set of input signals
    private HashSet<String> inputs;
    // Set of output signals
    private HashSet<String> outputs;

    // True if this file is main.txt, false otherwise
    private boolean isMain;

    /**
     * Prepares a new HDLSim.HDLModuleReader object with the specified file path.
     * Prefix should be the name of the module that is instantiated.
     * HDLSim.Signals is a reference to the HDLSim.Signals object of this project. Will be created
     * by HDLSim.HDLSim (main method)
     *
     * Throws FileNotFoundException if a nonexistent invalid file path is provided.
     */
    public HDLModuleReader(Signals signals, String dir, String fileName, String prefix) throws FileNotFoundException {
        this.dir = dir;
        this.prefix = prefix;
        this.file = new File(dir + "/" + fileName);
        this.sc = new Scanner(this.file);

        inputs = new HashSet<>();
        outputs = new HashSet<>();

        this.signals = signals;
    }

    /**
     * Reads the provided file and parses the logic to add to the HDLSim.Signals object
     * representing the logic.
     *
     * Upon encountering a submodule, creates a new HDLSim.HDLModuleReader for that module and
     * passes off execution. Creates bridge signals.
     *
     * Throws HDLException if any problem occurs while reading the HDLFile.
     */
    public void readModule() throws HDLException {
        boolean moduleDefinedYet = false;
        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            // Check for and skip empty lines
            if (!line.contains("//")) {
                if (line.trim().length() == 0) { continue; }
            } else {
                if (line.substring(0, line.indexOf("//")).trim().length() == 0) { continue; }
            }

            // Make sure only one module per file is present, and it is first
            String firstWord = line.split(" ")[0];
            if ((!moduleDefinedYet && !firstWord.equals("module")) ||
                    (moduleDefinedYet && firstWord.equals("module"))) {
                throw new HDLParseException("Each file must contain exactly one module definition as the block in the file");
            }
            switch (firstWord) {
                case "module" -> {
                    readLineModule(line);
                    moduleDefinedYet = true;
                }
                case "reg" -> readLineReg(line);
                case "wire" -> readLineWire(line);
                case "submod" -> readLineSubmod(line);
                case "TERMINATE" -> readLineTerminate(line);
                default -> readLineAssignment(line);
            }

        }

    }

    /**
     * Reads the provided line to parse the module declaration.
     * Will consume more lines until the module declaration is fully read.
     */
    private void readLineModule(String line) {
        String[] tokens = line.split(" ");
        isMain = tokens[1].equals("main");
        // Get full module description (all inputs and outputs)
        StringBuilder fullExpression = new StringBuilder(line);
        while (fullExpression.indexOf(";") < 0) {
            if (!sc.hasNextLine()) {
                throw new HDLParseException("Module declaration must end with a semicolon");
            }
            fullExpression.append(sc.nextLine() + " ");
        }
        String cxnsStr = fullExpression.substring(fullExpression.indexOf("(") + 1, fullExpression.indexOf(")"));
        if (cxnsStr.trim().length() == 0) { return; }
        for (String str : cxnsStr.split(",")) {
            String cxn = str.trim();
            String cxnType = cxn.substring(0, cxn.indexOf(" ")).trim();
            String cxnName = cxn.substring(cxn.indexOf(" ") + 1).trim();
            if (cxnType.equals("input")) {
                signals.addWire(prefix + cxnName);
                inputs.add(cxnName);
            }
            else if (cxnType.equals("output")) {
                signals.addWire(prefix + cxnName);
                outputs.add(cxnName);
            }
            else {
                throw new HDLParseException("All module connections must be declared 'input' or 'output'");
            }
        }
    }

    /**
     * Reads the provided line to initialize a reg.
     */
    private void readLineReg(String line) {
        StringBuilder fullExpression = new StringBuilder(line);
        while (fullExpression.indexOf(";") < 0) {
            if (!sc.hasNextLine()) {
                throw new HDLParseException("Register declaration must end with a semicolon");
            }
            fullExpression.append(sc.nextLine() + " ");
        }

        String[] tokens = fullExpression.toString().split(" ");
        int idx = tokens[3].indexOf(";");
        String valStr = idx >= 0 ? tokens[3].substring(0, idx) : tokens[3];
        Integer val = null;
        if (valStr.length() > 2) {
            if (valStr.startsWith("0x")) {
                val = Integer.parseInt(valStr.substring(2), 16);
            } else if (valStr.startsWith("0b")) {
                val = Integer.parseInt(valStr.substring(2), 2);
            }
        }
        if (val == null) val = Integer.parseInt(valStr);
        signals.addReg(prefix + tokens[1], val);
    }

    /**
     * Reads the provided line to initialize a wire
     */
    private void readLineWire(String line) {
        StringBuilder fullExpression = new StringBuilder(line);
        while (fullExpression.indexOf(";") < 0) {
            if (!sc.hasNextLine()) {
                throw new HDLParseException("Wire declaration must end with a semicolon");
            }
            fullExpression.append(sc.nextLine() + " ");
        }

        String rest = fullExpression.substring(line.indexOf("wire") + 4).trim();
        String name = rest.substring(0, rest.indexOf(";")).trim();
        signals.addWire(prefix + name);
    }

    /**
     * Reads the provided line to instantiate a declared submodule.
     * Will then switch execution over to a new HDLSim.HDLModuleReader which will
     * fully read that submodule.
     * Finally, creates bridge connections to set up the inputs and outputs
     * of the new submodule.
     * Will consume more lines until the submodule instantiation is fully parsed.
     */
    private void readLineSubmod(String line) {
        String[] tokens = line.split(" ");
        String type = tokens[1];
        String instanceName = tokens[2].replaceAll(" \\(\\)/", "");
        String subPrefix = prefix + instanceName + "/";
        HDLModuleReader submodReader;
        try {
            submodReader = new HDLModuleReader(signals, dir, type + ".txt", subPrefix);
        } catch (FileNotFoundException e) {
            throw new HDLException(String.format("HDL file for %s %s %s could not be found", "submod", type, instanceName));
        }
        submodReader.readModule();

        StringBuilder fullExpression = new StringBuilder(line);
        while (fullExpression.indexOf(";") < 0) {
            if (!sc.hasNextLine()) {
                throw new HDLParseException("Submodule instantiation must end with a semicolon");
            }
            fullExpression.append(sc.nextLine() + " ");
        }
        int startIdx = fullExpression.indexOf("(");
        int endIdx;
        for (endIdx = fullExpression.length() - 1; endIdx >= 0; endIdx--) {
            if (fullExpression.charAt(endIdx) == ')') break;
        }
        String cxnsStr = fullExpression.substring(startIdx + 1, endIdx);
        for (String str : cxnsStr.split(",")) {
            String cxn = str.trim();
            String pin = cxn.substring(cxn.indexOf(".") + 1, cxn.indexOf("(")).trim();
            String wireConnection = cxn.substring(cxn.indexOf("(") + 1, cxn.indexOf(")")).trim();

            if (submodReader.getInputs().contains(pin)) {
                signals.addExpression(subPrefix + pin, prefix + wireConnection);
            }
            else if (submodReader.getOutputs().contains(pin)) {
                signals.addExpression(prefix + wireConnection, subPrefix + pin);
            }
            else {
                throw new HDLException("Specified pin is not a part of this submodule");
            }
        }
    }

    /**
     * Reads a line to parse the TERMINATE statement of the HDL.
     */
    private void readLineTerminate(String line) {
        assert isMain : "TERMINATE statement can only be in the main file";

        StringBuilder fullExpression = new StringBuilder(line);
        while (fullExpression.indexOf(";") < 0) {
            if (!sc.hasNextLine()) {
                throw new HDLParseException("Terminate declaration must end with a semicolon");
            }
            fullExpression.append(sc.nextLine() + " ");
        }

        String rest = fullExpression.substring(line.indexOf("TERMINATE") + 9).trim();
        String removeSemicolon = rest.substring(0, rest.indexOf(";")).trim();
        String addParentheses = checkForAndAddParentheses(removeSemicolon);
        String addPrefixToVars = addParentheses.replaceAll("([a-zA-Z_][a-zA-Z0-9_]*)", prefix + "$1");
        signals.addTerminate(addPrefixToVars);
    }

    /**
     * Reads the line to parse an assignment of a variable to an expression.
     */
    private void readLineAssignment(String line) {
        StringBuilder fullExpression = new StringBuilder(line);
        while (fullExpression.indexOf(";") < 0) {
            if (!sc.hasNextLine()) {
                throw new HDLParseException("Assignment expression must end with a semicolon");
            }
            fullExpression.append(sc.nextLine() + " ");
        }

        String[] words = fullExpression.toString().split(" ");
        String firstWord = prefix + words[0];

        if (signals.getRegs().contains(firstWord)) {
            if (!words[1].equals("<=")) {
                throw new HDLParseException("Must use <= when assigning to regs!");
            }
        } else if (signals.getWires().contains(firstWord)) {
            if (!words[1].equals("=")) {
                throw new HDLParseException("Must use = when assigning to regs!");
            }
        }

        String rest = line.substring(line.indexOf("=")+1).trim();
        String removeSemicolon = rest.substring(0, rest.indexOf(";")).trim();
        String addParentheses = checkForAndAddParentheses(removeSemicolon);
        String addPrefixToVars = addParentheses.replaceAll("([a-zA-Z_][a-zA-Z0-9_]*)", prefix + "$1");
        signals.addExpression(firstWord, addPrefixToVars);
    }

    /**
     * Takes in EXP, an expression string, and adds outer parentheses to it
     * iff it is missing them.
     */
    public static String checkForAndAddParentheses(String exp) {
        boolean hasOperator = false;
        for (String op : Expression.VALID_OPERATORS) {
            if (exp.contains(op)) {
                hasOperator = true;
                break;
            }
        }
        if (!hasOperator) return exp;

        int openMinusClose = 0;
        for (int i = 0; i < exp.length(); i++) {
            char c = exp.charAt(i);
            if (c == '(') openMinusClose += 1;
            else if (c == ')') openMinusClose -= 1;

            // If there are no enclosing parentheses and we are not at the start/end of the string
            if (openMinusClose == 0 && i != 0 && i != exp.length()-1) {
                return "(" + exp + ")";
            }
        }

        return exp;
    }


    ///// GETTERS AND SETTERS /////////////////////////////////////////////////

    public HashSet<String> getInputs() {
        return inputs;
    }

    public HashSet<String> getOutputs() {
        return outputs;
    }

}
