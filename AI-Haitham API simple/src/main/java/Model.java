import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;


public class Model {

    private String APIKeyOwner = "DEEPSEEK_API_KEY";
    private String model = "deepseek-chat";
    private double temperature = 0.7;
    private String provider = null;
    private String apiURL = "https://api.deepseek.com/v1/chat/completions";
    private Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);

    private List<Message> conversationHistory;
    private Map<String, Object> requestBody;
    private HttpPost httpPost;
    private String systemPrompt = "";
    private int maxHistoryLength = 30;
    private int token_limit = 8100;
    private boolean stream = true;
    private boolean running = false;
    private String introduction = "Hi, how can I help?";
    private String inputIndication = ": ";
    private String speechIndication = ">_";

    public Model() throws JsonProcessingException {
        this.conversationHistory = new ArrayList<>();
        this.requestBody = new HashMap<>();

        this.initJson();

        conversationHistory.add(new Message("system", systemPrompt));
    }

    public void setInputIndication(String inputIndication) {
        this.inputIndication = inputIndication;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    // set system prompt
    public void setSystemPrompt(String prompt) {
        systemPrompt = prompt;
        conversationHistory.set(0, new Message("system",  prompt));
    }

    public void setMaxHistoryLength(int maxHistoryLength) {
        this.maxHistoryLength = maxHistoryLength;
    }

    // get API from Path
    private String API_KEY(){
        return  System.getenv(APIKeyOwner.trim());
    }

    // prepare http message
    private void initHTTPpost(){

        httpPost.setHeader("Content-Type", "application/json; charset=UTF-8");
        httpPost.setHeader("Accept", "application/json; charset=UTF-8");
        httpPost.setHeader("Authorization", "Bearer " + API_KEY().trim());

    }

    // prepare json body
    private void initJson() throws JsonProcessingException {

        requestBody.put("model", model);
        requestBody.put("max_tokens", token_limit);
        requestBody.put("temperature", temperature);
        requestBody.put("stream", stream);

        // if from hugging face or similar, provider is needed
        if (provider != null) {
            requestBody.put("provider", provider);
        }
    }

    private String getJson() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(requestBody);
    }

    private void maintainHistory(){
        if  (conversationHistory.size() >= maxHistoryLength) {
            conversationHistory.remove(1);
        }
    }

    // generates are returns newest user input, while also handling background processes
    public String generateText(String prompt, boolean print) throws IOException {
        conversationHistory.add(new Message("user", prompt));
        requestBody.put("messages", conversationHistory);

        String json = getJson();
        debugPrint("REQUEST BODY: " + json);

        //fresh httpPost
        httpPost = new HttpPost(apiURL);
        initHTTPpost();
        httpPost.setEntity(new StringEntity(getJson(), StandardCharsets.UTF_8));

        //sending API
        String response;
        if (print){
            System.out.print(speechIndication);
            response = Services.applyAPIRequest(httpPost, chunk -> System.out.print(chunk));
        } else {
            response = Services.applyAPIRequest(httpPost);
        }
        conversationHistory.add(new Message("assistant", response));
        debugPrint("added to history: " + response);
        maintainHistory();
        return response;
    }

    private void setToken_limit(int token_limit) {
        this.token_limit = token_limit;
    }

    private void processCommand(String[] command) throws IOException {
        switch (command[0]) {
            case "stop": running = false; break;
            case "set_history_length": setMaxHistoryLength(Integer.parseInt(command[1])); break;
            case "token_limit": setToken_limit(Integer.parseInt(command[1])); break;

        }
    }

    private void debugPrint(String message){
        // System.out.println(message);
    }

    public void setSpeechIndication(String newSpeechIndication) {
        this.speechIndication  = newSpeechIndication;
    }

    public void visit() throws IOException {
        System.out.println(introduction);
        running = true;
        while(running){
            System.out.print(inputIndication);
            String input = scanner.nextLine();
            debugPrint(input);
            if (input.charAt(0) == '/'){
                processCommand(input.substring(1).split(" "));
            }else{
                generateText(input, true);
                System.out.println(" ");
            }
        }
    }


}
