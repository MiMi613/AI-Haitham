

import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class Main {

    private static ArrayList<Model> characters = new ArrayList<>();

    public static void main(String[] args) throws IOException, URISyntaxException {
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));

        characters.add(AlHaitham());
        characters.add(Lisa());

        Scanner sc = new  Scanner(System.in);
        String host = sc.nextLine();
        switch (host){
            case "AlHaitham": characters.get(0).visit(); break;
            case "Lisa": characters.get(1).visit(); break;
            case "C": conversation(); break;
            default: System.out.println("Invalid input");
        }
    }

    static void conversation() throws IOException {
        Scanner sc = new  Scanner(System.in);
        System.out.print("Select the two speakers: ");
        int[] speakers = Arrays.stream(sc.nextLine().split(" ")).mapToInt(Integer::parseInt).toArray();
        Model s_1 = characters.get(speakers[0]);
        Model s_2 = characters.get(speakers[1]);

        // s_1 speaks first
        //s_1.generateText("Please start our conversation", true);
        String greeting = sc.nextLine();
        String r1 = s_1.generateText(greeting, true);;
        String r2;
        while(true){

            sc.nextLine();
            r2 = s_2.generateText(r1, true);

            sc.nextLine();
            r1 = s_1.generateText(r2, true);
        }

    }

    static Model AlHaitham() throws IOException {
        Model ret = new Model();
        String pathToCharacter = "HaithamPrompt.txt";
        String Prompt = Services.readFromPath(pathToCharacter);
        ret.setSystemPrompt(Prompt);
        ret.setIntroduction("I hope you hesitated before contacting me. What do you need?");
        ret.setSpeechIndication("Al-Haitham: ");
        ret.setTemperature(0.7);
        return ret;
    }

    static Model Lisa() throws IOException {
        Model ret = new Model();
        String pathToCharacter = "LisaPrompt.txt";
        String Prompt = Services.readFromPath(pathToCharacter);
        ret.setSystemPrompt(Prompt);
        ret.setIntroduction("Hey cutie, would you like to join me for an afternoon tea?");
        ret.setSpeechIndication("Lisa:       ");
        ret.setTemperature(0.8);
        return ret;
    }

}
