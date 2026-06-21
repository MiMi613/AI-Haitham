package ai.haitham.diagnostics;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class StreamDiagnostics {

    private final Path codepointsPath;
    private final Path rawJsonPath;

    public StreamDiagnostics(Path codepointsPath, Path rawJsonPath) {
        this.codepointsPath = codepointsPath;
        this.rawJsonPath = rawJsonPath;
    }

    /**
     * Persists codepoint and raw payload diagnostics for a single JSON chunk line.
     */
    public void recordPayload(String payload) throws IOException {
        StringBuilder hexDump = new StringBuilder();
        payload.codePoints().forEach(cp -> hexDump.append(String.format("U+%04X ", cp)));

        java.nio.file.Files.writeString(
                codepointsPath,
                hexDump.toString() + "\n",
                StandardCharsets.UTF_8,
                java.nio.file.StandardOpenOption.APPEND,
                java.nio.file.StandardOpenOption.CREATE
        );

        java.nio.file.Files.writeString(
                rawJsonPath,
                payload + "\n",
                StandardCharsets.UTF_8,
                java.nio.file.StandardOpenOption.APPEND,
                java.nio.file.StandardOpenOption.CREATE
        );
    }
}