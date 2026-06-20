package ai.haitham.app;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import ai.haitham.client.OllamaClient;
import ai.haitham.model.Model;
import ai.haitham.resource.ResourceService;
import ai.haitham.ui.ConsoleSessionRunner;

public class Main {

    private static final String CHARACTERS_DIRECTORY = "characters";
    private static final ArrayList<Model> characters = new ArrayList<>();
    private static final OllamaClient ollamaClient = new OllamaClient();
    private static final ConsoleSessionRunner consoleRunner = new ConsoleSessionRunner();

    /**
     * Entry point: initializes characters and routes control to a selected mode.
     */
    public static void main(String[] args) throws IOException {
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));

        characters.addAll(loadCharacters());

        try (Scanner sc = new Scanner(System.in, StandardCharsets.UTF_8)) {
            String host = sc.nextLine();
            if ("C".equals(host)) {
                conversation();
                return;
            }

            for (Model character : characters) {
                if (character.getName().equals(host)) {
                    consoleRunner.visit(character);
                    return;
                }
            }

            System.out.println("Invalid input");
        }
    }

    /**
     * Loads every character configuration file from the characters resource directory.
     */
    static List<Model> loadCharacters() throws IOException {
        ArrayList<Model> loadedCharacters = new ArrayList<>();
        List<String> fileNames = ResourceService.listClasspathDirectory(CHARACTERS_DIRECTORY);
        fileNames.sort(Comparator.naturalOrder());
        for (String fileName : fileNames) {
            loadedCharacters.add(loadCharacter(fileName));
        }
        return loadedCharacters;
    }

    /**
     * Reads a character file using Scanner delimiters and returns a configured model.
     */
    static Model loadCharacter(String characterFileName) throws IOException {
        String content = ResourceService.readClasspathResource(CHARACTERS_DIRECTORY + "/" + characterFileName);

        Map<String, String> fields = new LinkedHashMap<>();

        try (Scanner scanner = new Scanner(content)) {
            scanner.useDelimiter("\\R");

            while (scanner.hasNext()) {
                String line = scanner.next().stripTrailing();

                if (line.isBlank() || line.startsWith("#")) {
                    continue;
                }

                try (Scanner lineScanner = new Scanner(line)) {
                    lineScanner.useDelimiter("\\s*[:=]\\s*");
                    if (!lineScanner.hasNext()) {
                        continue;
                    }

                    String key = lineScanner.next().trim();
                    String value = lineScanner.hasNext() ? lineScanner.next() : "";
                    fields.put(key, value);
                }
            }
        }

        String client = fields.getOrDefault("client", "ollama").trim().toLowerCase();
        if (!"ollama".equals(client)) {
            throw new IllegalArgumentException("Unsupported client '" + client + "' in character file: " + characterFileName);
        }

        Model model = new Model(ollamaClient);
        model.setName(fields.getOrDefault("name", characterFileName));
        model.setOllamaModel(fields.getOrDefault("model", "llama3.2:latest"));

        String temperature = fields.getOrDefault("temperature", "0.7");
        model.setTemperature(Double.parseDouble(temperature));

        model.setIntroduction(fields.getOrDefault("introduction", "Hi, how can I help?"));
        model.setSpeechIndication(fields.getOrDefault("speechIndication", ">_"));
        model.setInputIndication(fields.getOrDefault("inputIndication", ": "));

        String systemPromptFile = fields.get("systemPromptFile");
        if (systemPromptFile == null || systemPromptFile.isBlank()) {
            throw new IllegalArgumentException("Character file must define systemPromptFile: " + characterFileName);
        }
        model.setSystemPrompt(ResourceService.readClasspathResource(systemPromptFile));
        return model;
    }

    /**
     * Runs a turn-based conversation loop between two selected character models.
     */
    static void conversation() throws IOException {
        try (Scanner sc = new Scanner(System.in, StandardCharsets.UTF_8)) {
            System.out.print("Select the two speakers: ");
            int[] speakers = Arrays.stream(sc.nextLine().split(" ")).mapToInt(Integer::parseInt).toArray();
            Model s1 = characters.get(speakers[0]);
            Model s2 = characters.get(speakers[1]);

            String greeting = sc.nextLine();
            System.out.print(s1.getSpeechIndication());
            String r1 = s1.generateText(greeting, chunk -> System.out.print(chunk));
            System.out.println(" ");

            String r2;
            while (true) {
                sc.nextLine();
                System.out.print(s2.getSpeechIndication());
                r2 = s2.generateText(r1, chunk -> System.out.print(chunk));
                System.out.println(" ");

                sc.nextLine();
                System.out.print(s1.getSpeechIndication());
                r1 = s1.generateText(r2, chunk -> System.out.print(chunk));
                System.out.println(" ");
            }
        }
    }

}