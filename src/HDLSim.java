import Exceptions.HDLException;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;

/**
 * @author Jonah Tharakan
 *
 * Driver class. Contains main method.
 */

public class HDLSim {

    private static HashMap<String, Boolean> flags;
    private static String[] allFlags = {"d", "v", "help"};
    private static String[] flagNames = {"debug", "verbose", "help"};

    private static File sourceDir;

    public static void main(String[] args) throws FileNotFoundException {
        parseCommand(args);
        Signals signals = new Signals();
        HDLModuleReader mainReader = new HDLModuleReader(
                signals, sourceDir.getPath(), "main.txt", "/");
        mainReader.readModule();
        signals.build();
        signals.stepToTerminate();
    }

    /**
     * Valid arg formats:
     * HDLSim <source dir> [flags]
     */
    private static void parseCommand(String[] args) {
        // Get source dir
        String sourceDirStr = args[0];
        sourceDir = new File(sourceDirStr);
        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            throw new IllegalArgumentException("Invalid project directory specified");
        }

        // Get flags
        flags = new HashMap<>();
        for (String flagName : flagNames) {
            flags.put(flagName, false);
        }
        for (int i = 1; i < args.length; i++) {
            String arg = args[i];
            if (arg.charAt(0) == '-') {
                boolean foundFlag = false;
                String rest = arg.substring(1);
                for (int j = 0; j < allFlags.length; j++) {
                    if (rest.equals(allFlags[j]) || rest.equals(flagNames[j])) {
                        flags.put(flagNames[j], true);
                        foundFlag = true;
                        break;
                    }
                }
                if (!foundFlag) { throw new IllegalArgumentException("Undefined flag specified"); }
            }
        }
    }

    public static boolean checkFlag(String flag) {
        if (flags.containsKey(flag)) { return flags.get(flag); }
        else { throw new IllegalArgumentException("Undefined flag specified"); }
    }

}
