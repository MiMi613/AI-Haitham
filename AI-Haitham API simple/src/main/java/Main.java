import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException, URISyntaxException {
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));

        Model AlHaitham = AlHaitham();
        Model Lisa = Lisa();

        Scanner sc = new  Scanner(System.in);
        String host = sc.nextLine();
        switch (host){
            case "AlHaitham": AlHaitham.visit(); break;
            case "Lisa": Lisa.visit(); break;
            default: System.out.println("Invalid input");
        }
    }

    static Model AlHaitham() throws IOException {
        Model ret = new Model();
        String pathToCharacter = "HaithamPrompt.txt";
        String Prompt = Services.readFromPath(pathToCharacter);
        ret.setSystemPrompt(Prompt);
        ret.setIntroduction("I hope you hesitated before contacting me. What do you need?");
        ret.setSpeechIndication("Al-Haitham: ");
        return ret;
    }

    static Model Lisa() throws IOException {
        Model ret = new Model();
        String pathToCharacter = "LisaPrompt.txt";
        String Prompt = Services.readFromPath(pathToCharacter);
        ret.setSystemPrompt(Prompt);
        ret.setIntroduction("Hey cutie, would you like to join me for an afternoon tea?");
        ret.setSpeechIndication("Lisa:       ");
        return ret;
    }

}
