import Exceptions.HDLException;
import Exceptions.HDLParseException;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * @author Jonah Tharakan
 *
 * Class that takes in the path to a valid HDL txt file and reads it to
 * construct a Signals object that encapsulates the logic in the file.
 */

public class HDLFileReader {

    private File file;
    private Scanner sc;

    /**
     * Prepares a new HDLFileReader object with the specified file path.
     *
     * Throws FileNotFoundException if a nonexistent invalid file path is provided.
     */
    public HDLFileReader(String filePath) throws FileNotFoundException {
        this.file = new File(filePath);
        this.sc = new Scanner(this.file);
    }

    /**
     * Reads the provided file and parses the logic to create a Signals object
     * representing the logic.
     *
     * Returns that created Signals object.
     *
     * Throws HDLException if any problem occurs while reading the HDLFile.
     */
    public Signals readFile() throws HDLException {
        Signals signals = new Signals();

        // TODO: Support multiline statements
        // Read all lines into Signals
        while (sc.hasNextLine()) {
            String line = sc.nextLine().trim();
            String firstWord = line.split(" ")[0];
            String rest;
            String removeSemicolon;
            String addParentheses;
            String[] words;
            switch (firstWord) {
                case "reg":
                    words = line.split(" ");
                    int idx = words[3].indexOf(";");
                    String valStr = idx >= 0 ? words[3].substring(0, idx) : words[3];
                    Integer val = null;
                    if (valStr.length() > 2) {
                        if (valStr.startsWith("0x")) {
                            val = Integer.parseInt(valStr.substring(2), 16);
                        } else if (valStr.startsWith("0b")) {
                            val = Integer.parseInt(valStr.substring(2), 2);
                        }
                    }
                    if (val == null) val = Integer.parseInt(valStr);
                    signals.addReg(words[1], val);
                    break;

                case "wire":
                    rest = line.substring(line.indexOf("wire") + 4).trim();
                    removeSemicolon = rest.substring(0, rest.indexOf(";")).trim();
                    signals.addWire(removeSemicolon);
                    break;

                case "TERMINATE":
                    rest = line.substring(line.indexOf("TERMINATE") + 9).trim();
                    removeSemicolon = rest.substring(0, rest.indexOf(";")).trim();
                    addParentheses = checkForAndAddParentheses(removeSemicolon);
                    signals.addTerminate(addParentheses);
                    break;

                default:
                    words = line.split(" ");

                    if (signals.getRegs().contains(firstWord)) {
                        if (!words[1].equals("<=")) {
                            throw new HDLParseException("Must use <= when assigning to regs!");
                        }
                    } else if (signals.getWires().contains(firstWord)) {
                        if (!words[1].equals("=")) {
                            throw new HDLParseException("Must use = when assigning to regs!");
                        }
                    }

                    rest = line.substring(line.indexOf("=")+1).trim();
                    removeSemicolon = rest.substring(0, rest.indexOf(";")).trim();
                    addParentheses = checkForAndAddParentheses(removeSemicolon);
                    signals.addExpression(firstWord, addParentheses);
            }
        }

        // Build to generate wireOrder
        signals.build();

        return signals;
    }

    /**
     * Takes in EXP, an expression string, and adds outer parentheses to it
     * iff it is missing them.
     */
    private String checkForAndAddParentheses(String exp) {
        for (int i = 0; i < exp.length() - 1; i++) {
            if (Expression.VALID_OPERATORS.contains(exp.substring(i,i+1))) {
                return exp;
            } else if (Expression.VALID_OPERATORS.contains(exp.substring(i,i+2))) {
                return exp;
            }
        }

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

}
