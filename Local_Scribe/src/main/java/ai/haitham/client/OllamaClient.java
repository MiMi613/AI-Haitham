package ai.haitham.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.function.Consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class OllamaClient {

    private static final ObjectMapper mapper = new ObjectMapper();

    private final HttpClient httpClient;
    private final URI ollamaGenerateUri;

    public OllamaClient() {
        this("http://127.0.0.1:11434");
    }

    public OllamaClient(String baseUrl) {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.ollamaGenerateUri = URI.create(baseUrl + "/api/generate");
    }

    /**
     * Sends a prompt to Ollama and returns the generated response text.
     */
    public String generate(String model, String prompt, String systemPrompt, double temperature, boolean stream) throws IOException {
        return generate(model, prompt, systemPrompt, temperature, stream, null);
    }

    /**
     * Sends a prompt to Ollama and optionally streams response chunks to a callback.
     */
    public String generate(String model, String prompt, String systemPrompt, double temperature, boolean stream, Consumer<String> onChunk) throws IOException {
        if (model == null || model.isBlank()) {
            throw new IllegalArgumentException("Ollama model cannot be null or blank");
        }

        ObjectNode payload = mapper.createObjectNode();
        payload.put("model", model.trim());
        payload.put("prompt", prompt);
        payload.put("system", systemPrompt);
        payload.put("stream", stream);

        ObjectNode options = mapper.createObjectNode();
        options.put("temperature", temperature);
        payload.set("options", options);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(ollamaGenerateUri)
                .timeout(Duration.ofMinutes(5))
                .header("Content-Type", "application/json; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(payload.toString(), StandardCharsets.UTF_8))
                .build();

        try {
            HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() != 200) {
                String errorBody = new String(response.body().readAllBytes(), StandardCharsets.UTF_8);
                throw new IOException("Ollama request failed with status " + response.statusCode() + " at " + ollamaGenerateUri + ". Response: " + errorBody);
            }

            StringBuilder content = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.body(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.isBlank()) {
                        continue;
                    }

                    JsonNode chunkNode = mapper.readTree(line);
                    JsonNode responseNode = chunkNode.path("response");
                    if (!responseNode.isMissingNode() && !responseNode.isNull()) {
                        String chunk = responseNode.asText();
                        content.append(chunk);
                        if (onChunk != null) {
                            onChunk.accept(chunk);
                        }
                    }
                }
            }

            return content.toString();
        } catch (IOException e) {
            if (e.getMessage() != null && e.getMessage().startsWith("Ollama request failed with status")) {
                throw e;
            }
            throw new IOException("Failed to reach Ollama at " + ollamaGenerateUri + ". Ensure Ollama is running and the model '" + model.trim() + "' is available (try: ollama list).", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Ollama request interrupted", e);
        }
    }
}