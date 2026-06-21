package ai.haitham.service;

import ai.haitham.client.OllamaClient;
import ai.haitham.model.Model;
import ai.haitham.resource.ResourceService;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
public class CharacterService {

    private static final String CHARACTERS_DIR = "characters";

    /** Cached raw properties from .character files, keyed by lowercase name. */
    private final Map<String, Map<String, String>> characterProps = new LinkedHashMap<>();
    private final List<Map<String, String>> characterList = new ArrayList<>();
    private final OllamaClient ollamaClient = new OllamaClient();

    @PostConstruct
    public void init() throws IOException {
        List<String> files = ResourceService.listClasspathDirectory(CHARACTERS_DIR);
        files.sort(Comparator.naturalOrder());
        for (String fileName : files) {
            String content = ResourceService.readClasspathResource(CHARACTERS_DIR + "/" + fileName);
            Map<String, String> props = parseProperties(content);
            String name = props.getOrDefault("name", fileName);
            characterProps.put(name.toLowerCase(), props);
            characterList.add(props);
        }
    }

    /** Returns lightweight character info for the frontend. */
    public List<Map<String, String>> getAll() {
        List<Map<String, String>> result = new ArrayList<>();
        for (Map<String, String> props : characterList) {
            Map<String, String> info = new LinkedHashMap<>();
            info.put("name", props.getOrDefault("name", ""));
            info.put("introduction", props.getOrDefault("introduction", ""));
            result.add(info);
        }
        return result;
    }

    public Optional<Map<String, String>> getProps(String name) {
        return Optional.ofNullable(characterProps.get(name.toLowerCase()));
    }

    /**
     * Creates a fresh Model with its own conversation history from the character template.
     */
    public Model createSession(String name) throws IOException {
        Map<String, String> props = characterProps.get(name.toLowerCase());
        if (props == null) {
            return null;
        }
        return buildModel(props);
    }

    private Model buildModel(Map<String, String> props) throws IOException {
        Model model = new Model(ollamaClient);
        model.setName(props.getOrDefault("name", ""));
        model.setOllamaModel(props.getOrDefault("model", "llama3.2:latest"));
        model.setIntroduction(props.getOrDefault("introduction", "Hi, how can I help?"));
        model.setSpeechIndication(props.getOrDefault("speechIndication", ">_"));
        model.setInputIndication(props.getOrDefault("inputIndication", ": "));

        String tempStr = props.get("temperature");
        if (tempStr != null) {
            model.setTemperature(Double.parseDouble(tempStr));
        }

        String promptFile = props.get("systemPromptFile");
        if (promptFile != null) {
            model.setSystemPrompt(ResourceService.readClasspathResource(promptFile));
        }

        return model;
    }

    private Map<String, String> parseProperties(String content) {
        Map<String, String> props = new LinkedHashMap<>();
        for (String line : content.split("\n")) {
            int colon = line.indexOf(':');
            if (colon > 0) {
                String key = line.substring(0, colon).trim();
                String value = line.substring(colon + 1).trim();
                props.put(key, value);
            }
        }
        return props;
    }
}
