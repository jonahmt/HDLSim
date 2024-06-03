
import java.io.File;
import java.io.FileNotFoundException;
import java.time.Instant;
import java.util.HashMap;

/**
 * @author Jonah Tharakan
 *
 * Driver class. Contains main method.
 */

public class HDLSim {

    // Stores whether a flag is active or not in this execution
    private static HashMap<String, Boolean> flags;
    // List of valid flag abbreviations
    private static final String[] allFlags = {"v", "help", "x", "nl", "t"};
    // List of flag full names. Indices line up with allFlags.
    private static final String[] flagNames = {"verbose", "help", "hex", "no-log", "time"};

    // HDL source code directory for this execution
    private static File sourceDir;

    // Start time of execution
    private static long startTime;

    public static void main(String[] args) throws FileNotFoundException {
        parseCommand(args);

        if (HDLSim.checkFlag("time")) {
            startTime = Instant.now().toEpochMilli();
        }

        Signals signals = new Signals();
        signals.setOutputDir("test/TestSourceFiles/03_basic_module_test/out");
        HDLModuleReader mainReader = new HDLModuleReader(
                signals, sourceDir.getPath(), "main.txt", "/");
        mainReader.readModule();
        signals.build();
        signals.stepToTerminate();
        signals.dumpFinalOutput();
        signals.cleanUp();

        if (HDLSim.checkFlag("time")) {
            double millis = (Instant.now().toEpochMilli() - startTime) / 1000.0d;
            System.out.println("Execution time: " + millis + "s");
        }

        System.out.println("Finished Execution Successfully");
    }

    /**
     * Reads the source directory and flags, setting the appropriate static variables
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

        if (checkFlag("help")) {
            printHelpMessage();
            System.exit(0);
        }
    }

    /**
     * Returns the truth value of the flag.
     *
     * @throws IllegalArgumentException if an invalid flag is provided.
     */
    public static boolean checkFlag(String flag) {
        if (flags.containsKey(flag)) { return flags.get(flag); }
        else { throw new IllegalArgumentException("Undefined flag specified"); }
    }

    /**
     * Prints message for -help command
     */
    private static void printHelpMessage() {
        String msg =
        """
        --- HELP MESSAGE GUIDE ---
        
        Usage:
            HDLSim <source directory> [flags]
        
        Available Flags:
            FLAG    : WORD      : DESCRIPTION
            ----------------------------------
            -v      : verbose   : TBD.
            -help   : help      : Prints this message.
            -x      : hex       : Causes output file values to be displayed in hex rather than decimal.
            -nl     : no-log    : Does not dump values to intermediate log. Final values will still be dumped. Should improve speed.
            -t      : time      : Prints the run time of execution.
        """;

        System.out.println(msg);
    }

}
