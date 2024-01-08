import Exceptions.HDLException;

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
        while (sc.hasNextLine()) {
            String line = sc.nextLine().trim();
            String firstWord = line.split(" ")[0];
            String rest;
            String removeSemicolon;
            switch (firstWord) {
                case "reg":
                    String[] words = line.split(" ");
                    String valStr = words[3].substring(0, words[3].indexOf(";"));
                    if (valStr.length() > 2) {
                        if (valStr.substring(0, 2).equals("0x")) {
                            //16
                        } else if () {
                          // 2
                        }
                    }
                    // 10

                    break;
                case "wire":
                    rest = line.substring(line.indexOf("wire") + 4).trim();

                    break;
                case "TERMINATE":
                    rest = line.substring(line.indexOf("TERMINATE") + 9).trim();
                    removeSemicolon = rest.substring(0, rest.indexOf(";"));
                    signals.addTerminate(removeSemicolon);
                    break;
                default:

            }
        }

        return signals;
    }

}
