package cz.lukaskabc.cvut.processor.configuration.tests;


import cz.lukaskabc.cvut.processor.ConfigurationDocProcessor;
import lombok.Getter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.opentest4j.AssertionFailedError;

import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static cz.lukaskabc.cvut.processor.ProcessorConfiguration.PROCESSOR_CONFIGURATION_PREFIX;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractProcessorTest {
    public static final String PACKAGE_NAME = "cz.lukaskabc.cvut.processor.configuration.tests";
    public final String sourceDirectory;
    public final boolean keepFile;

    public final JavaCompiler compiler;
    public final StandardJavaFileManager fileManager;

    @Getter
    private String fileName;

    public AbstractProcessorTest() {
        var sourceDirectoryENV = System.getenv("SOURCE_DIRECTORY");

        compiler = ToolProvider.getSystemJavaCompiler();
        Assertions.assertNotNull(compiler);

        fileManager = compiler.getStandardFileManager(null, null, StandardCharsets.UTF_8);
        Assertions.assertNotNull(fileManager);

        if (sourceDirectoryENV == null) {
            sourceDirectory = "src/main/java";
            System.err.println("SOURCE_DIRECTORY not set, using default value: " + sourceDirectory);
        } else {
            sourceDirectory = sourceDirectoryENV;
        }


        Assertions.assertFalse(sourceDirectory.isBlank());

        keepFile = Boolean.parseBoolean(System.getenv("KEEP_FILE"));
    }

    @BeforeEach
    public void generateFileName() {
        this.fileName = "annotationprocessor-test-output-" + UUID.randomUUID();
        System.out.println("File name: " + fileName);
    }

    public List<String> mapProcessorOptions(String[] options) {
        List<String> list = new ArrayList<>();
        for (var option : options) {
            list.add("-A" + PROCESSOR_CONFIGURATION_PREFIX + option);
        }
        return list;
    }

    private List<String> mapFilesToPaths(List<String> fileNames) {
        var folderName = getFolderName().isBlank() ? "" : getFolderName() + "/";
        return fileNames.stream().map(s -> sourceDirectory + "/" + PACKAGE_NAME.replace('.', '/') + "/" + folderName + s + ".java").toList();
    }

    public TransparentDiagnosticCollector compileWithProcessor(List<String> fileNames, String format, String... processorOptions) {
        var diagnosticCollector = new TransparentDiagnosticCollector();

        var options = new ArrayList<>(List.of(
                "-encoding", "UTF-8",
                "-proc:only",
                "-processor", ConfigurationDocProcessor.class.getName() + ",lombok.launch.AnnotationProcessorHider$AnnotationProcessor",
                "-d", "target/tests/classes"
        ));
        options.addAll(mapProcessorOptions(processorOptions));
        options.addAll(mapProcessorOptions(new String[]{
                "output_file=" + this.fileName + "." + format,
                "format=" + format
        }));

        var compilationUnits = fileManager.getJavaFileObjectsFromStrings(mapFilesToPaths(fileNames));
        var task = compiler.getTask(
                null,
                fileManager,
                diagnosticCollector,
                options,
                null,
                compilationUnits
        );

        boolean success = false;
        try {
            success = task.call();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

        try {
            fileManager.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Assertions.assertTrue(success);
        return diagnosticCollector;
    }

    public void documentFile(String fileName, String format, Integer expectedValuesAmount) {
        documentFile(fileName, format, expectedValuesAmount, new String[0]);
    }

    public void documentFile(String fileName, String format, Integer expectedValuesAmount, String... args) {
        documentFiles(List.of(fileName), format, expectedValuesAmount, args);
    }

    public void documentFiles(List<String> fileNames, String format, Integer expectedValuesAmount, String... args) {
        var logs = compileWithProcessor(fileNames, format, args);
        Assertions.assertEquals(expectedValuesAmount, InTotalDocumented(logs));
    }

    public int InTotalDocumented(TransparentDiagnosticCollector logs) {
        var msg = logs.getDiagnostics().stream().map(m -> m.getMessage(null)).filter(f -> f.startsWith("In total ")).findAny();
        if (msg.isEmpty()) return -1;
        var split = msg.get().split(" ");


        Assertions.assertTrue(split.length > 2);
        try {
            return Integer.parseInt(split[2]);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    @AfterEach
    public void deleteFile() {
        if (keepFile)
            return;

        if (fileName != null && !fileName.isBlank()) {
            System.out.println("Removing file " + fileName);
            new File(fileName).delete();
            new File(fileName + ".md").delete();
            new File(fileName + ".html").delete();
        }
    }

    public void validateFiles(String expectedFileName, String extension) {

        var folderName = getFolderName().isBlank() ? "" : getFolderName() + "/";
        var resourceName = "/configuration_tests_expected_outputs/" + extension + "/" + folderName + expectedFileName + "." + extension;
        var resource = AbstractProcessorTest.class.getResource(resourceName);
        Assertions.assertNotNull(resource);

        String actual;
        String expected;
        int line = 0;
        try (var actualFile = new BufferedReader(new FileReader(fileName + "." + extension));
             var expectedFile = new BufferedReader(new FileReader(Paths.get(resource.toURI()).toFile()))) {
            Assertions.assertNotNull(actualFile);
            Assertions.assertNotNull(expectedFile);

            while ((expected = expectedFile.readLine()) != null) {
                actual = actualFile.readLine();
                line++;
                Assertions.assertEquals(expected, actual);
            }

            Assertions.assertNull(actualFile.readLine());
            Assertions.assertNull(expectedFile.readLine());
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        } catch (AssertionFailedError e) {
            System.err.println("Actual file: " + fileName + "." + extension);
            System.err.println("Expected file: " + resource.getPath());
            System.err.println("Line: " + line);
            throw e;
        }
    }

    protected String getFolderName() {
        return "";
    }

    public void testFile(String fileName, String extension, Integer expectedValuesAmount) {
        documentFile(fileName, extension, expectedValuesAmount, new String[0]);
        validateFiles(fileName, extension);
    }
}
