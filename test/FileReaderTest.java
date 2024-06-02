import Exceptions.HDLException;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;

import static org.junit.jupiter.api.Assertions.*;

public class FileReaderTest {

    @Test
    public void testRegex() {
        String prefix = "/";
        String toProcess = "(C_hello_2 + B + 3 + _D)";
        String processed = toProcess.replaceAll("([a-zA-Z_][a-zA-Z0-9_]*)", prefix + "$1");
        System.out.println(processed);
    }

    @Test
    public void doWhatIWant() {
        Signals signals = new Signals();
        try {
            HDLModuleReader reader = new HDLModuleReader(signals, "test/TestSourceFiles/03_basic_module_test", "main.txt", "/");
            reader.readModule();
        }
        catch (Exception e) {
            e.printStackTrace();
            fail();
        }
        System.out.println("done reading");
        signals.build();
        System.out.println("done building");
        signals.stepToTerminate();
        System.out.println(signals.getValues().toString());
    }

}
