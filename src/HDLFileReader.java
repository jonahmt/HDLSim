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

    private String filePath;
    private File file;
    private Scanner sc;

    /**
     * Prepares a new HDLFileReader object with the specified file path.
     * Throws FileNotFoundException if a nonexistent invalid file path is provided.
     */
    public HDLFileReader(String filePath) throws FileNotFoundException {
        this.filePath = filePath;
        this.file = new File(filePath);
        this.sc = new Scanner(this.file);
    }

    /**
     * Reads the provided file and parses the logic to create a Signals object
     * representing the logic.
     *
     * Returns that created Signals object.
     */
    public Signals readFile() {
        Signals signals = new Signals();

        // TODO: Support multiline statements
        while (sc.hasNextLine()) {
            String line = sc.nextLine().trim();
            String firstWord = line.split(" ")[0];
            switch (firstWord) {
                case "reg":

                    break;
                case "wire":

                    break;
                case "TERMINATE":

                    break;
                default:

            }
        }

        return signals;
    }

}
