package ai.haitham.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import ai.haitham.client.OllamaClient;

public class Model {

    private static final String DEFAULT_OLLAMA_MODEL = "llama3.2:latest";

    private final OllamaClient ollamaClient;
    private final List<Message> conversationHistory;

    private double temperature = 0.7;
    private String name = "";
    private String ollamaModel = DEFAULT_OLLAMA_MODEL;
    private String systemPrompt = "";
    private final boolean stream = true;

    private String introduction = "Hi, how can I help?";
    private String inputIndication = ": ";
    private String speechIndication = ">_";

    /**
     * Creates a new chat model instance and initializes conversation history
     * with an empty system message placeholder.
     */
    public Model(OllamaClient ollamaClient) {
        this.ollamaClient = Objects.requireNonNull(ollamaClient, "ollamaClient");
        this.conversationHistory = new ArrayList<>();
        conversationHistory.add(new Message("system", systemPrompt));
    }

    /**
     * Replaces the active system prompt and updates the first history entry
     * so subsequent generation uses the new instructions.
     */
    public void setSystemPrompt(String prompt) {
        systemPrompt = prompt;
        conversationHistory.set(0, new Message("system", prompt));
    }

    /**
     * Sends a user prompt to Ollama and stores both user and assistant messages
     * in local conversation history.
     */
    public String generateText(String prompt) throws IOException {
        return generateText(prompt, null);
    }

    /**
     * Sends a user prompt to Ollama, optionally forwarding streamed chunks to
     * the provided callback while still saving full history.
     */
    public String generateText(String prompt, Consumer<String> onChunk) throws IOException {
        conversationHistory.add(new Message("user", prompt));

        String response = ollamaClient.generate(
                ollamaModel,
                prompt,
                systemPrompt,
                temperature,
                stream,
                onChunk
        );

        conversationHistory.add(new Message("assistant", response));
        return response;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public void setName(String name) {
        this.name = name == null ? "" : name.trim();
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public void setInputIndication(String inputIndication) {
        this.inputIndication = inputIndication;
    }

    public void setSpeechIndication(String newSpeechIndication) {
        this.speechIndication = newSpeechIndication;
    }

    public void setOllamaModel(String ollamaModel) {
        if (ollamaModel == null || ollamaModel.isBlank()) {
            throw new IllegalArgumentException("Ollama model cannot be null or blank");
        }
        this.ollamaModel = ollamaModel.trim();
    }

    public String getOllamaModel() {
        return ollamaModel;
    }

    public String getName() {
        return name;
    }

    public String getIntroduction() {
        return introduction;
    }

    public String getInputIndication() {
        return inputIndication;
    }

    public String getSpeechIndication() {
        return speechIndication;
    }
}