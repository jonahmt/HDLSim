import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;

/**
 * @author Jonah Tharakan
 *
 * Class that stores the data for many files as String arrays.
 */
public class ModuleFileCache {

    private HashMap<String, List<String>> data;

    /**
     * Constructs a module file cache given an array of strings representing file names.
     */
    public ModuleFileCache(String[] fileNames) throws FileNotFoundException {
        BufferedReader reader;
        for (String fileName : fileNames) {
            reader = new BufferedReader(new FileReader(fileName));

        }
    }

}
