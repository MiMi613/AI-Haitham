# Learning Scribe — Full-Stack Web App

Spring Boot 3.4 backend + React 18 frontend. The only webapp in this repository.

## Structure
```
Learning_Scribe/
├── frontend/                    ← React UI (Vite, port 5173)
│   ├── src/App.jsx             ← Terminal chat interface
│   ├── src/App.css             ← Dark terminal theme
│   └── vite.config.js          ← Proxies /api → localhost:8080
├── src/main/java/ai/haitham/   ← Spring Boot backend (port 8080)
│   ├── AiHaithamApplication.java
│   ├── controller/             ← CharacterController, ChatController
│   ├── service/                ← CharacterService
│   ├── config/                 ← CorsConfig
│   ├── client/                 ← OllamaClient, StreamingApiClient
│   ├── model/                  ← Model, Message
│   └── resource/               ← ResourceService
├── src/main/resources/
│   ├── characters/             ← .character config files
│   └── prompts/                ← system prompt .txt files
└── pom.xml                     ← Spring Boot 3.4.1, Java 23
```

## Conventions
- Java 23, Spring Boot 3.4.1, Maven, UTF-8
- React 18, Vite 6
- Tabs for indentation
- Backend is stateless — fresh `Model` per chat request
- SSE streaming via `SseEmitter` for real-time responses
- CORS configured for React dev server (port 5173)

## Build & Run
```powershell
# Backend (terminal 1)
cd Learning_Scribe
mvn spring-boot:run                    # → http://localhost:8080

# Frontend (terminal 2)
cd Learning_Scribe/frontend
npm run dev                            # → http://localhost:5173
```

## API
- `GET /api/characters` — list available characters
- `POST /api/chat/{characterName}` — send message, receive SSE stream
