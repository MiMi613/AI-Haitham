# Local Scribe

Standalone Java console app. Character-based AI chat using Ollama. Manages character configs, conversation history, and streaming responses.

## Structure
- `src/main/java/ai/haitham/app/` — `Main.java` entry point
- `src/main/java/ai/haitham/client/` — `OllamaClient`, `StreamingApiClient`
- `src/main/java/ai/haitham/model/` — `Model`, `Message`
- `src/main/java/ai/haitham/resource/` — `ResourceService` (classpath file loading)
- `src/main/java/ai/haitham/ui/` — `ConsoleSessionRunner`
- `src/main/java/ai/haitham/diagnostics/` — `StreamDiagnostics`
- `src/main/resources/characters/` — `.character` config files
- `src/main/resources/prompts/` — system prompt `.txt` files

## Conventions
- Java 23, Maven, UTF-8
- Package root: `ai.haitham`
- Tabs for indentation
- Character configs are `key: value` format
- `Model` holds conversation history, temperature, Ollama model ref

## Build & Run
```powershell
cd Local_Scribe
mvn compile exec:java -Dexec.mainClass="ai.haitham.app.Main"
```
