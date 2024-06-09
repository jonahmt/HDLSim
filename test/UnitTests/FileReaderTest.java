package UnitTests;

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

}
