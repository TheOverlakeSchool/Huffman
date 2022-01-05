import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Scanner;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HuffmanTest {

    private ByteArrayOutputStream baos;

    @ParameterizedTest
    @MethodSource("testFiles")
    void testSave(String filename) throws IOException {
        int[] frequencies = getFrequencies(filename + ".txt");
        HuffmanCode huffmanCode = new HuffmanCode(frequencies);
        PrintStream outputStream = outputStream();
        huffmanCode.save(outputStream);

        String[] actual = baos.toString().split("\n\r?");
        Scanner expected = expectedFileContents(filename + ".code");
        for (String line : actual) {
            assertEquals(expected.nextLine(), line);
        }
        assertFalse(expected.hasNext());
    }

    @ParameterizedTest
    @MethodSource("testFiles")
    void testTranslate(String filename) throws IOException, URISyntaxException {
        Scanner codeInput = new Scanner(getClass().getClassLoader().getResourceAsStream(filename + ".code"));
        HuffmanCode t = new HuffmanCode(codeInput);
        InputStream code = getClass().getClassLoader().getResourceAsStream(filename + ".code");

        URL resource = getClass().getClassLoader().getResource(filename + ".short");
        File compressedFile = new File(resource.toURI());

        BitInputStream input = new BitInputStream(compressedFile);
        PrintStream outputStream = outputStream();
        t.translate(input, outputStream);
        outputStream.close();
        String[] actual = baos.toString().split("\n\r?");
        Scanner expected = expectedFileContents(filename + ".txt");
        for (String line : actual) {
            assertEquals(expected.nextLine(), line);
        }
        assertFalse(expected.hasNext());
    }

    private Scanner expectedFileContents(String filename) {
        return new Scanner(getClass().getClassLoader().getResourceAsStream(filename));
    }

    private static Stream<Arguments> testFiles() {
        return Stream.of(
                Arguments.of("tiny"),
                Arguments.of("taxi"),
                Arguments.of("simple"),
                Arguments.of("short"),
                Arguments.of("short256"),
                Arguments.of("hamlet")
        );
    }

    private PrintStream outputStream() {
        baos = new ByteArrayOutputStream();
        return new PrintStream(baos);
    }
    
    private int[] getFrequencies(String filename) throws IOException {
        InputStream input = getClass().getClassLoader().getResourceAsStream(filename);
        int[] count = new int[255];
        int n = input.read();
        while (n != -1) {
            count[n]++;
            n = input.read();
        }
        return count;
    }
}