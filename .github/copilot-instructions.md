# Copilot Instructions — AI-Haitham Workspace

This workspace contains three **independent** projects. Each has its own `.github/copilot-instructions.md` with project-specific details. Read the relevant one before working on a project.

## Projects

| Project | Type | Framework | Instructions |
|---|---|---|---|
| `AI-Haitham API simple/` | Java CLI | Plain Java + HttpClient | `.github/copilot-instructions.md` |
| `Local_Scribe/` | Java CLI | Plain Java + Ollama | `.github/copilot-instructions.md` |
| `Learning_Scribe/` | Backend API | Spring Boot 3.4 | `.github/copilot-instructions.md` |
| `AI-Haitham Webapp/` | Frontend | React 18 + Vite | _(inline below)_ |

## Workspace conventions
- No project depends on any other — they share zero code
- Tabs for indentation (all Java projects)
- Java 23 across all Java projects

## Frontend — AI-Haitham Webapp
- React 18, Vite 6
- Terminal-like dark UI (monospace, #0d1114 background)
- SSE streaming from backend for chat
- Dev server on port 5173, proxies `/api` to `localhost:8080`

```powershell
cd "AI-Haitham Webapp" && npm run dev
```

