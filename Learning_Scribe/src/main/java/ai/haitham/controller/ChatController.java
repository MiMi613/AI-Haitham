package ai.haitham.controller;

import ai.haitham.model.Model;
import ai.haitham.service.CharacterService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final CharacterService characterService;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public ChatController(CharacterService characterService) {
        this.characterService = characterService;
    }

    @PostMapping(value = "/{characterName}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(
            @PathVariable String characterName,
            @RequestBody Map<String, String> request
    ) {
        String message = request.getOrDefault("message", "").trim();
        if (message.isEmpty()) {
            throw new IllegalArgumentException("Message cannot be empty");
        }

        final Model session;
        try {
            session = characterService.createSession(characterName);
        } catch (IOException e) {
            SseEmitter errorEmitter = new SseEmitter();
            errorEmitter.completeWithError(e);
            return errorEmitter;
        }
        if (session == null) {
            SseEmitter errorEmitter = new SseEmitter();
            errorEmitter.completeWithError(new IllegalArgumentException("Character not found: " + characterName));
            return errorEmitter;
        }

        SseEmitter emitter = new SseEmitter(300_000L); // 5 min timeout

        executor.execute(() -> {
            try {
                session.generateText(message, chunk -> {
                    try {
                        emitter.send(SseEmitter.event().data(chunk));
                    } catch (IOException e) {
                        // Client disconnected
                    }
                });
                emitter.send(SseEmitter.event().data("[DONE]"));
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }
}
