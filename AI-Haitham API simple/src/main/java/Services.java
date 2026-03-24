import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;


public class Services {

    private static CloseableHttpClient httpClient = HttpClients.createDefault();

    public static String applyAPIRequest(HttpPost httpPost) throws IOException {

        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {

            if (response.getStatusLine().getStatusCode() != 200) {
                throw new RuntimeException("API request failed: " + response.getStatusLine());
            }

            InputStreamReader isr = new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(isr);
            debugPrint("Reader encoding: " + isr.getEncoding()); // Should print: UTF8

            ObjectMapper mapper = new ObjectMapper();
            StringBuilder content = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                if (!line.startsWith("data: ") || line.equals("data: [DONE]")) continue;

                JsonNode delta = mapper.readTree(line.substring(6).getBytes(StandardCharsets.UTF_8))
                        .path("choices").get(0).path("delta").path("content");

                if (!delta.isMissingNode()) {

                    debugPrint("Node type: " + delta.getNodeType());
                    debugPrint("Node raw: " + delta.toString());

                    String chunk = delta.textValue();

                    content.append(chunk);
                }
            }

            return content.toString();   // full string for conversation history

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String applyAPIRequest(HttpPost httpPost, Consumer<String> onChunk) throws IOException {

        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {

            if (response.getStatusLine().getStatusCode() != 200) {
                throw new RuntimeException("API request failed: " + response.getStatusLine());
            }

            InputStreamReader isr = new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(isr);
            debugPrint("Reader encoding: " + isr.getEncoding()); // Should print: UTF8

            ObjectMapper mapper = new ObjectMapper();
            StringBuilder content = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                if (!line.startsWith("data: ") || line.equals("data: [DONE]")) continue;

                // DIAGNOSTIC 1: Check raw line for question marks
                {
                    debugPrint("Raw line codepoints: ");
                    //line.codePoints().forEach(cp -> System.out.printf("U+%04X ", cp));
                    debugPrint("");
                }

                // File viewing
                {
                    StringBuilder hexDump = new StringBuilder();
                    line.substring(6).codePoints().forEach(cp ->
                            hexDump.append(String.format("U+%04X ", cp))
                    );
                    Files.writeString(
                            Path.of("codepoints.txt"),
                            hexDump.toString() + "\n",
                            StandardCharsets.UTF_8,
                            java.nio.file.StandardOpenOption.APPEND,
                            java.nio.file.StandardOpenOption.CREATE
                    );
                    Files.writeString(
                            Path.of("rawjson.txt"),
                            line.substring(6) + "\n",
                            StandardCharsets.UTF_8,
                            java.nio.file.StandardOpenOption.APPEND,
                            java.nio.file.StandardOpenOption.CREATE
                    );
                }


                // CORRECT — explicitly give Jackson UTF-8 bytes:
                JsonNode delta = mapper.readTree(line.substring(6).getBytes(StandardCharsets.UTF_8))
                        .path("choices").get(0).path("delta").path("content");



                if (!delta.isMissingNode()) {

                    debugPrint("Node type: " + delta.getNodeType());
                    debugPrint("Node raw: " + delta.toString());

                    String chunk = delta.textValue();

                    // DIAGNOSTIC 2: Check chunk after Jackson parsing
                    {
                        debugPrint("Chunk codepoints: ");
                        //chunk.codePoints().forEach(cp -> System.out.printf("U+%04X ", cp));
                        debugPrint("");
                    }

                    content.append(chunk);
                    onChunk.accept(chunk);
                }
            }

            //writeToPath("output.txt", content.toString());
            return content.toString();   // full string for conversation history

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void writeToPath(String path, String content) throws URISyntaxException, IOException {
        URL location = Services.class.getProtectionDomain().getCodeSource().getLocation();
        Path classPath = Paths.get(location.toURI());
        Path resourcePath = classPath.resolve(path).normalize();

        Files.createDirectories(resourcePath.getParent());
        Files.write(resourcePath, content.getBytes(StandardCharsets.UTF_8));

        System.out.println("Successfully wrote to " + resourcePath.toString());
    }

    public static String readFromPath(String path) throws URISyntaxException, IOException {

        try {
            URL location = Services.class.getProtectionDomain().getCodeSource().getLocation();
            Path classPath = Paths.get(location.toURI());
            Path resourcePath = classPath.resolve(path).normalize();

            String content = new String(Files.readAllBytes(resourcePath), StandardCharsets.UTF_8);
            return content;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void debugPrint(String message) {
        //System.out.println(message);
    }
}
