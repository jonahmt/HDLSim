import java.io.File;

/**
 * @author Jonah Tharakan
 *
 * Class that takes in the path to a valid HDL txt file and reads it to
 * construct a Signals object that encapsulates the logic in the file.
 */

public class HDLFileReader {

    String filePath;
    File file;

    public HDLFileReader(String filePath) {
        this.filePath = filePath;
        this.file = new File(filePath);
    }



}
