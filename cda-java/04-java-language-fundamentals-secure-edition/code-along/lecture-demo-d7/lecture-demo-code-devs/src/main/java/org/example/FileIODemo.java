package org.example;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class FileIODemo {

    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Path logFile;

    public FileIODemo(Path logFile) {
        // STEP 1: Path represents a file location; it does not create the file yet.
        this.logFile = logFile;
    }

    public void logMessage(String message) throws IOException {
        if (message == null || message.isBlank()) {
            // STEP 2: Throw a specific custom exception when the input is invalid.
            throw new InvalidLogMessageException("Log message must not be blank");

        }

        writeLogEntry("INFO", message);
    }

    public void logException(Exception exception) throws IOException {
        // STEP 3: Record the exception type and message instead of silently
        // swallowing the error. Real applications may also include a stack trace.
        String exceptionDetails = exception.getClass().getSimpleName()
                + ": "
                + exception.getMessage();

        writeLogEntry("ERROR", exceptionDetails);
    }

    private void writeLogEntry(String level, String message) throws IOException {
        // STEP 4: A file may have a parent folder that does not exist yet.
        // createDirectories is safe even when the folder already exists.
        Path parentDirectory = logFile.getParent();
        if (parentDirectory != null) {
            Files.createDirectories(parentDirectory);
        }

        // STEP 5: Build the text for one log entry.
        String logEntry = "["
                + LocalDateTime.now().format(TIMESTAMP_FORMAT)
                + "] ["
                + level
                + "] "
                + message;

        // STEP 6: Open a BufferedWriter inside a try-with-resources statement.
        // CREATE makes the file when needed, and APPEND preserves earlier messages.
        try (BufferedWriter writer = Files.newBufferedWriter(
                logFile,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
        )) {
            writer.write(logEntry);
            writer.newLine();
        }

        // writer.close() is called automatically when the try block ends,
        // even when an IOException occurs inside the block.
    }

    public List<String> readMessages() throws IOException {
        // STEP 7: Check for the file before reading it.
        // An unused logger returns an empty list instead of causing a missing-file error.
        if (Files.notExists(logFile)) {
            return List.of();
        }

        List<String> messages = new ArrayList<>();

        // STEP 8: BufferedReader reads text efficiently without loading the entire
        // file at once. try-with-resources automatically closes the reader.
        try (BufferedReader reader = Files.newBufferedReader(logFile, StandardCharsets.UTF_8))
        {
            String line;

            // readLine returns one line at a time and null at the end of the file.
            while ((line = reader.readLine()) != null) {
                messages.add(line);
            }
        }

        return messages;
    }

    public static void main(String[] args) {
        // STEP 9: Resolve a file inside a "logs" folder in the project directory.
        FileIODemo logger = new FileIODemo(Path.of("logs", "application.log"));

//        FileIODemo logger = new FileIODemo(
//                Path.of("C:\\ScratchPad\\logsDemo\\application.log")  // OR Path.of("C:/ScratchPad/logsDemo/application.log")

//        );

        try {
            logger.logMessage("The application started.");

            try {
                // STEP 10: This invalid value deliberately triggers our custom exception.
                logger.logMessage(" ");
            } catch (InvalidLogMessageException exception) {
                // STEP 11: Catch the custom exception and write it to the same log file.
                logger.logException(exception);
                System.out.println("The custom exception was written to the log.");
            }

            System.out.println("Current log contents:");

            //logger.readMessages().forEach(System.out::println); //shorthand syntax using lambda
            for (String message : logger.readMessages()){
                System.out.println(message);
            }

        } catch (IOException exception) {
            // File operations can fail because of permissions, invalid paths,
            // or hardware problems. Report the error rather than silently ignoring it.
            System.err.println("Unable to use the log file: " + exception.getMessage());
        }
    }
}

