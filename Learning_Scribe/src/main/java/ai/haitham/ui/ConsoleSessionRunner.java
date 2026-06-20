package ai.haitham.ui;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import ai.haitham.model.Model;

public class ConsoleSessionRunner {

    /**
     * Starts an interactive console session for the provided model.
     */
    public void visit(Model model) throws IOException {
        try (Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8)) {
            System.out.println(model.getIntroduction());
            boolean running = true;

            while (running) {
                System.out.print(model.getInputIndication());
                String input = scanner.nextLine();
                if (input.isEmpty()) {
                    continue;
                }

                if (input.charAt(0) == '/') {
                    running = processCommand(input.substring(1).split(" "));
                } else {
                    System.out.print(model.getSpeechIndication());
                    model.generateText(input, chunk -> System.out.print(chunk));
                    System.out.println(" ");
                }
            }
        }
    }

    private boolean processCommand(String[] command) {
        return !"stop".equals(command[0]);
    }
}