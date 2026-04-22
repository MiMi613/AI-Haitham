import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


public class Model {

    private String APIKeyOwner = "DEEPSEEK_API_KEY";
    private String model = "deepseek-chat";
    private double temperature = 0.7;
    private String provider = null;
    private String apiURL = "https://api.deepseek.com/v1/chat/completions";
    private Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);
    private ModelApplication modelApplication;

    private List<Message> conversationHistory;
    private Map<String, Object> requestBody;
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
        this.modelApplication = new ApiModelApplication();

        this.initJson();

        conversationHistory.add(new Message("system", systemPrompt));
    }

    public void setInputIndication(String inputIndication) {
        this.inputIndication = inputIndication;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
        requestBody.put("temperature", temperature);
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

    public void setModelApplication(ModelApplication modelApplication) {
        this.modelApplication = Objects.requireNonNull(modelApplication, "modelApplication");
    }

    // get API from Path
    private String API_KEY(){
        return  System.getenv(APIKeyOwner.trim());
    }

    public String authorizationHeader() {
        return "Bearer " + API_KEY().trim();
    }

    public String getApiURL() {
        return apiURL;
    }

    // prepare json body
    private void initJson() throws JsonProcessingException {
        refreshRequestBodyConfig();
    }

    private void refreshRequestBodyConfig() {
        requestBody.put("model", model);
        requestBody.put("max_tokens", token_limit);
        requestBody.put("temperature", temperature);
        requestBody.put("stream", stream);

        if (provider != null) {
            requestBody.put("provider", provider);
        } else {
            requestBody.remove("provider");
        }
    }

    public String getJson() throws JsonProcessingException {
        refreshRequestBodyConfig();
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

        String response = modelApplication.call(this, print);
        conversationHistory.add(new Message("assistant", response));
        debugPrint("added to history: " + response);
        maintainHistory();
        return response;
    }

    private void setToken_limit(int token_limit) {
        this.token_limit = token_limit;
        requestBody.put("max_tokens", token_limit);
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

    public String getSpeechIndication() {
        return speechIndication;
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
