package ai.haitham.app;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import ai.haitham.client.OllamaClient;
import ai.haitham.model.Model;
import ai.haitham.resource.ResourceService;
import ai.haitham.ui.ConsoleSessionRunner;

public class Main {

    private static final ArrayList<Model> characters = new ArrayList<>();
    private static final OllamaClient ollamaClient = new OllamaClient();
    private static final ConsoleSessionRunner consoleRunner = new ConsoleSessionRunner();

    /**
     * Entry point: initializes characters and routes control to a selected mode.
     */
    public static void main(String[] args) throws IOException {
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));

        characters.add(alHaitham());
        characters.add(lisa());

        try (Scanner sc = new Scanner(System.in, StandardCharsets.UTF_8)) {
            String host = sc.nextLine();
            switch (host) {
                case "AlHaitham" -> consoleRunner.visit(characters.get(0));
                case "Lisa" -> consoleRunner.visit(characters.get(1));
                case "C" -> conversation();
                default -> System.out.println("Invalid input");
            }
        }
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

    /**
     * Builds and configures the Al-Haitham character model.
     */
    static Model alHaitham() throws IOException {
        Model ret = new Model(ollamaClient);
        String prompt = ResourceService.readClasspathResource("HaithamPrompt.txt");
        ret.setSystemPrompt(prompt);
        ret.setIntroduction("I hope you hesitated before contacting me. What do you need?");
        ret.setSpeechIndication("Al-Haitham: ");
        ret.setTemperature(0.7);
        return ret;
    }

    /**
     * Builds and configures the Lisa character model.
     */
    static Model lisa() throws IOException {
        Model ret = new Model(ollamaClient);
        String prompt = ResourceService.readClasspathResource("LisaPrompt.txt");
        ret.setSystemPrompt(prompt);
        ret.setIntroduction("Hey cutie, would you like to join me for an afternoon tea?");
        ret.setSpeechIndication("Lisa:       ");
        ret.setTemperature(0.8);
        return ret;
    }
}