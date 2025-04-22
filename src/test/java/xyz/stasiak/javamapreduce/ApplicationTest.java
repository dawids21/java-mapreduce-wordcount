package xyz.stasiak.javamapreduce;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.logging.LogManager;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import xyz.stasiak.javamapreduce.processing.Controller;
import xyz.stasiak.javamapreduce.processing.ProcessingStatus;
import xyz.stasiak.javamapreduce.processing.Server;
import xyz.stasiak.javamapreduce.rmi.RemoteServer;
import xyz.stasiak.javamapreduce.util.FilesUtil;
import xyz.stasiak.javamapreduce.util.SystemProperties;
import xyz.stasiak.javamapreduce.wordcount.cli.Command;
import xyz.stasiak.javamapreduce.wordcount.cli.CommandWithArguments;

class ApplicationTest {

    record TestFile(String name, String content) {
    }

    private static final String INPUT_NAME = "junit/input";
    private static final String OUTPUT_NAME = "junit/output";
    private Path inputDir;
    private Path outputDir;
    private RemoteServer server;

    @BeforeAll
    static void setUpLogging() throws IOException {
        LogManager.getLogManager().readConfiguration(
                ApplicationTest.class.getClassLoader().getResourceAsStream("logging.properties"));
    }

    @BeforeEach
    void setUp() throws Exception {
        inputDir = Path.of(FilesUtil.getFilesDirectory(INPUT_NAME));
        outputDir = Path.of(FilesUtil.getFilesDirectory(OUTPUT_NAME));

        Files.createDirectories(inputDir);
        Files.createDirectories(outputDir);

        initApplication();
        var port = Integer.parseInt(SystemProperties.getRmiPort());
        Registry rmiRegistry = LocateRegistry.getRegistry(port);
        server = (RemoteServer) rmiRegistry.lookup("server");
    }

    @AfterEach
    void tearDown() throws IOException {
        FilesUtil.deleteDirectory(inputDir.getParent());
    }

    private void createTestFiles(List<TestFile> files) throws IOException {
        for (var file : files) {
            var path = inputDir.resolve(file.name);
            Files.writeString(path, file.content);
        }
    }

    @Test
    void shouldProcessWordCount() throws IOException, InterruptedException, NotBoundException {
        var testFiles = List.of(
                new TestFile("file1.txt", "hello world\nworld hello\nhello hello"),
                new TestFile("file2.txt", "mapreduce test\ntest mapreduce\nerlang"));
        createTestFiles(testFiles);

        var startCommand = createStartCommand();

        var processingId = server.startProcessing(startCommand.toProcessingParameters());

        var status = waitForCompletion(processingId);

        assertEquals(ProcessingStatus.FINISHED, status);
    }

    private CommandWithArguments createStartCommand() {
        return new CommandWithArguments(
                Command.START,
                List.of(INPUT_NAME, OUTPUT_NAME, TestMapper.class.getName(),
                        TestReducer.class.getName()),
                "start " + INPUT_NAME + " " + OUTPUT_NAME + " " + TestMapper.class.getName() + " "
                        + TestReducer.class.getName());
    }

    private ProcessingStatus waitForCompletion(int processingId) throws InterruptedException, IOException {
        var status = ProcessingStatus.NOT_STARTED;
        var attempts = 0;
        var maxAttempts = 10;

        while (status != ProcessingStatus.FINISHED && attempts < maxAttempts) {
            Thread.sleep(1000);
            status = server.getProcessingStatus(processingId);
            attempts++;
        }

        return status;
    }

    private void initApplication() throws Exception {
        var rmiPort = Integer.parseInt(SystemProperties.getRmiPort());
        Registry rmiRegistry = LocateRegistry.createRegistry(rmiPort);
        Controller controller = new Controller();
        rmiRegistry.rebind("node", controller);
        server = new Server(controller);
        rmiRegistry.rebind("server", server);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                if (controller != null) {
                    controller.shutdownExecutor();
                    rmiRegistry.unbind("node");
                }
                if (server != null) {
                    rmiRegistry.unbind("server");
                }
            } catch (Exception e) {
            }
        }));
    }

}
