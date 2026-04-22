package ai.haitham.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.function.Consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ai.haitham.diagnostics.StreamDiagnostics;

public class StreamingApiClient {

    private static final ObjectMapper mapper = new ObjectMapper();

    private final HttpClient httpClient;

    public StreamingApiClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    /**
     * Executes a streaming request and returns accumulated content.
     */
    public String execute(HttpRequest request) throws IOException {
        return execute(request, null, null);
    }

    /**
     * Executes a streaming request, forwarding parsed chunks to callback.
     */
    public String execute(HttpRequest request, Consumer<String> onChunk) throws IOException {
        return execute(request, onChunk, null);
    }

    /**
     * Executes a streaming request, optionally forwarding chunks and diagnostics.
     */
    public String execute(HttpRequest request, Consumer<String> onChunk, StreamDiagnostics diagnostics) throws IOException {
        try {
            HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() != 200) {
                String errorBody = new String(response.body().readAllBytes(), StandardCharsets.UTF_8);
                throw new IOException("API request failed with status " + response.statusCode() + ". Response: " + errorBody);
            }

            StringBuilder content = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.body(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.startsWith("data: ") || line.equals("data: [DONE]")) {
                        continue;
                    }

                    String payload = line.substring(6);
                    if (diagnostics != null) {
                        diagnostics.recordPayload(payload);
                    }

                    JsonNode delta = mapper.readTree(payload.getBytes(StandardCharsets.UTF_8))
                            .path("choices").get(0).path("delta").path("content");

                    if (!delta.isMissingNode() && !delta.isNull()) {
                        String chunk = delta.textValue();
                        content.append(chunk);
                        if (onChunk != null) {
                            onChunk.accept(chunk);
                        }
                    }
                }
            }

            return content.toString();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Streaming API request interrupted", e);
        }
    }
}