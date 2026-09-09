#!/usr/bin/env python3
"""Append Appendices C and D (API samples, screenshots/ops notes)."""
from pathlib import Path
ROOT = Path("/Users/mishrapawas/Documents/class/2nd Year project")
OUT = ROOT / "docs" / "PROJECT_REPORT_FULL.md"
C = """

---

## Appendix C — Sample API Requests and Responses

### C.1 Submit a DELAY task (201 Created)

Request:

```
POST /api/tasks
Content-Type: application/json

{
  "type": "DELAY",
  "name": "Demo delay",
  "priority": "NORMAL",
  "delayMillis": 0,
  "timeoutMillis": 30000,
  "maxRetries": 0,
  "durationMillis": 2000
}
```

Response:

```json
{
  "id": "3f9c1a2b-4d5e-6f70-8091-a2b3c4d5e6f7",
  "name": "Demo delay",
  "type": "DELAY",
  "priority": "NORMAL",
  "status": "QUEUED",
  "attempt": 0,
  "maxRetries": 0,
  "timeoutMillis": 30000,
  "createdAt": "2026-09-09T10:00:00Z",
  "startedAt": null,
  "completedAt": null,
  "result": null,
  "error": null,
  "eventLog": ["10:00:00 CREATED type=DELAY priority=NORMAL", "10:00:00 QUEUED seq=41"]
}
```

### C.2 Same task after completion (GET /api/tasks/{id})

```json
{
  "id": "3f9c1a2b-4d5e-6f70-8091-a2b3c4d5e6f7",
  "status": "COMPLETED",
  "startedAt": "2026-09-09T10:00:00Z",
  "completedAt": "2026-09-09T10:00:02Z",
  "result": "Slept 2000 ms on scheduler-worker-2",
  "eventLog": ["10:00:00 CREATED ...", "10:00:00 QUEUED seq=41", "10:00:00 RUNNING worker=scheduler-worker-2", "10:00:02 COMPLETED Slept 2000 ms on scheduler-worker-2"]
}
```

### C.3 Validation error (400)

```
POST /api/tasks  { "type": "DELAY", "timeoutMillis": 10 }
```

```json
{ "error": "VALIDATION_FAILED", "message": "timeoutMillis: must be >= 100" }
```

### C.4 Unknown task (404)

```
GET /api/tasks/00000000-0000-0000-0000-000000000000
```

```json
{ "error": "TASK_NOT_FOUND", "message": "No task with id 00000000-...", "taskId": "00000000-..." }
```

### C.5 Burst submission (POST /api/burst?count=10)

```json
{ "submitted": 10, "ids": ["...", "..."] }
```

### C.6 Statistics (GET /api/stats)

```json
{
  "total": 14, "pending": 0, "queued": 1, "running": 2,
  "completed": 9, "failed": 1, "timeout": 0, "cancelled": 1, "retrying": 0,
  "queueDepth": 1, "workers": 4,
  "avgLatencyMs": 812.4, "p95LatencyMs": 2010.0,
  "byType": { "DELAY": 6, "COMPUTE": 3, "FETCH": 3, "BUILD_REPORT": 2 }
}
```

### C.7 SSE frames (GET /api/events)

```
event: task
data: {"id":"...","status":"PENDING","type":"DELAY","priority":"NORMAL",...}

event: task
data: {"id":"...","status":"QUEUED",...}

event: task
data: {"id":"...","status":"RUNNING","workerId":"scheduler-worker-1",...}

event: task
data: {"id":"...","status":"COMPLETED","result":"Slept 2000 ms ...",...}
```

---

## Appendix D — Screenshots and Operational Notes

### D.1 How to run (development)

Terminal 1 — backend:

```
cd backend
mvn spring-boot:run
```

Wait for `Started AsyncTaskSchedulerApp` on port 8080. Terminal 2 — frontend:

```
cd frontend
npm install
npm run dev
```

Open `http://localhost:5173`. The Vite proxy forwards `/api/*` to `http://localhost:8080`, so no CORS setup is needed in development.

### D.2 How to run (production jars/bundles)

```
cd backend && mvn package -DskipTests && java -jar target/*.jar
cd frontend && npm run build   # serve dist/ with any static server
```

### D.3 Suggested screenshots for the printed report

1. Dashboard on load (empty state + live badge).
2. Dashboard mid-burst (mixed PENDING/QUEUED/RUNNING/COMPLETED rows).
3. Detail modal showing a full event log.
4. Stats bar after a retry demonstration (RETRYING then FAILED/COMPLETED).
5. Terminal showing `mvn test` green (7/7) and `npm run build` clean.

### D.4 Troubleshooting

| Symptom | Cause | Fix |
|---|---|---|
| Blank dark page | Old cached bundle | Hard refresh (Cmd/Ctrl+Shift+R) |
| `ERR_CONNECTION_REFUSED` on /api | Backend not running | Start `mvn spring-boot:run` first |
| Port 8080 busy | Stale Spring process | `lsof -ti:8080 | xargs kill -9` |
| Port 5173 busy | Stale Vite process | `pkill -f vite`, restart `npm run dev` |
| Tasks stuck PENDING | Dispatcher starved (rare) | Restart backend; check `scheduler.workers` |

*End of report.*
"""
with open(OUT, "a", encoding="utf-8") as f:
    f.write(C)
print("Appendices C+D appended")
