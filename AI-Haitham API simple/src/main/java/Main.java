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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws IOException, URISyntaxException {
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));

        Model AlHaitham = new Model();
        String pathToCharacter = "HaithamPrompt.txt";
        String Prompt = Services.readFromPath(pathToCharacter);
        AlHaitham.setSystemPrompt(Prompt);
        AlHaitham.setIntroduction("I hope you hesitated before contacting me. What do you need?");
        System.out.println("Welcome to the Al-Haitham API");
        AlHaitham.visit();
    }

}
