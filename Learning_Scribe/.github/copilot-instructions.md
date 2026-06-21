# Learning Scribe — Backend

Spring Boot 3.4 backend for the AI-Haitham web application. Serves REST API + SSE streaming to the React frontend.

## Structure
- `src/main/java/ai/haitham/` — `AiHaithamApplication.java` (Spring Boot entry)
- `src/main/java/ai/haitham/controller/` — `CharacterController`, `ChatController`
- `src/main/java/ai/haitham/service/` — `CharacterService`
- `src/main/java/ai/haitham/config/` — `CorsConfig`
- `src/main/java/ai/haitham/client/` — `OllamaClient`, `StreamingApiClient`
- `src/main/java/ai/haitham/model/` — `Model`, `Message`
- `src/main/java/ai/haitham/resource/` — `ResourceService`
- `src/main/resources/characters/` — `.character` config files
- `src/main/resources/prompts/` — system prompt `.txt` files

## Conventions
- Java 23, Spring Boot 3.4.1, Maven, UTF-8
- Package root: `ai.haitham`
- Tabs for indentation
- Backend is stateless — fresh `Model` per chat request
- SSE streaming via `SseEmitter` for real-time responses
- CORS configured for React dev server (port 5173)

## Build & Run
```powershell
cd Learning_Scribe
mvn spring-boot:run
```
Runs on `http://localhost:8080`.

## API
- `GET /api/characters` — list available characters
- `POST /api/chat/{characterName}` — send message, receive SSE stream
