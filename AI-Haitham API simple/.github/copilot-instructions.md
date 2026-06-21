# AI-Haitham API Simple

Standalone Java CLI project. Calls Ollama (local) and DeepSeek (remote) APIs directly via `java.net.http.HttpClient`. No Spring, no framework.

## Structure
- `src/main/java/` — all `.java` files (default package)
- `src/main/resources/` — prompts, character configs, static files

## Conventions
- Java 23, Maven, UTF-8
- Default package (no `ai.haitham` prefix)
- `Main.java` is the entry point
- `Model.java` / `Message.java` — data classes
- `Services.java` — API orchestration

## Build & Run
```powershell
cd "AI-Haitham API simple"
mvn compile exec:java -Dexec.mainClass="Main"
```
