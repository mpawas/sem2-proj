# ⚡ Async Task Scheduler

> **2nd Year Project** · Advanced Java concurrency core + React live dashboard

A full-stack **asynchronous task scheduler** built with **advanced Java** (Spring Boot 3 + `java.util.concurrent`) and a **React** monitoring dashboard. Tasks are submitted over a REST API, wait in a **priority queue** with configurable delay, execute **asynchronously** on a worker pool, and stream their every state transition to the browser in real time over **Server-Sent Events (SSE)**.

---

## ✨ Screenshot placeholder

> Add a screenshot of the dashboard here when you run it (see *Quick start*).

## 🚀 Key features

| Feature | Where it lives |
| --- | --- |
| Delayed, asynchronous task execution | `ScheduledExecutorService` → priority queue → worker pool |
| Priority scheduling (CRITICAL > HIGH > NORMAL > LOW, FIFO within a level) | `PriorityBlockingQueue` |
| Thread-safe task registry, lock-free reads | `ConcurrentHashMap` |
| Live dashboard updates (SSE push) | `SseEmitter` + React `EventSource` |
| Execution timeouts | Scheduled timeout guards |
| Automatic retries with **exponential back-off** | `ScheduledExecutorService` + attempt counter |
| Cooperative cancellation (PENDING / QUEUED / RUNNING) | `volatile` cancel flag observed by jobs |
| Demo workload types | `DELAY`, `COMPUTE` (prime sieve), `FETCH`, `BUILD_REPORT` |
| Producer–consumer burst demo | `LinkedBlockingQueue` + consumer thread |
| CPU-bound aggregate report | Parallel `Stream`/`parallelStream()` |
| Validation, error contract, CORS | Spring MVC + `@RestControllerAdvice` |

## 🧰 Tech stack

| Layer | Technology |
| --- | --- |
| Backend | Java 17, Spring Boot 3.2, Maven |
| Concurrency API | `ExecutorService`, `ScheduledExecutorService`, `PriorityBlockingQueue`, `ConcurrentHashMap`, `CopyOnWriteArrayList`, `AtomicLong`, `LinkedBlockingQueue` |
| Frontend | React 18, Vite 5 (JavaScript/JSX), plain CSS |
| Realtime | Server-Sent Events (`text/event-stream`) |
| Tests | JUnit 5, AssertJ, Awaitility, Spring Boot Test |

---

## 📁 Repository layout

```
.
├── backend/                          # Java / Spring Boot application
│   ├── pom.xml
│   └── src
│       ├── main
│       │   ├── java/com/taskscheduler
│       │   │   ├── AsyncTaskSchedulerApp.java   # entry point
│       │   │   ├── common/                      # TaskStatus, TaskPriority, TaskType
│       │   │   ├── model/                       # TaskRecord (thread-safe state), DTOs
│       │   │   ├── scheduler/                   # ★ THE CORE
│       │   │   │   ├── TaskSchedulerService.java
│       │   │   │   ├── PriorityTaskQueue.java · QueuedTask.java
│       │   │   │   ├── DemoJobFactory.java
│       │   │   │   ├── SchedulerMetrics.java · SchedulerProperties.java
│       │   │   └── web/                         # REST + SSE controllers, error advice
│       │   └── resources/application.yml
│       └── test/java/com/taskscheduler
│           └── TaskSchedulerIntegrationTest.java
├── frontend/                          # React / Vite dashboard
│   ├── package.json · vite.config.js · index.html
│   └── src
│       ├── main.jsx                   # app orchestration + SSE subscription
│       ├── api.js                     # REST + EventSource client
│       ├── status.js · styles.css
│       └── components/                # TaskForm, TaskTable, StatsBar,
│                                      # TaskDetailModal, StatusBadge
├── docs/
│   ├── DESIGN.md                      # in-depth design & advanced-Java catalogue
│   └── PROJECT_REPORT.md              # submission report template
└── README.md                          # this file
```

---

## 🔧 Prerequisites

- **Java 17+** (tested with JDK 21) — `java -version`
- **Maven 3.8+** — `mvn -version`
- **Node.js 18+** — `node --version`

## ▶️ Quick start

**1) Backend (`http://localhost:8080`)**
```bash
cd backend
mvn spring-boot:run
```
Optional tweaks live in `backend/src/main/resources/application.yml`
(`scheduler.workers`, `queue-capacity`, `default-timeout-ms`, …).

**2) Frontend (`http://localhost:5173`)**
```bash
cd frontend
npm install
npm run dev
```
Open <http://localhost:5173>. Vite proxies `/api/*` to the backend, and the
dashboard subscribes to `/api/events` (SSE) for live updates.

> **Try this:** submit a **Delay** task with `durationMs: 6000` and a **Compute**
> task with `limit: 30000000` — watch them tick PENDING → QUEUED →
## 🎮 Demo script (curl)

```bash
BASE=http://localhost:8080/api/tasks

# 1. A delayed 3 s task, normal priority
curl -s -X POST "$BASE" -H 'Content-Type: application/json' -d '{
  "name":"nightly-export",
  "type":"DELAY",
  "priority":"NORMAL",
  "delayMillis":2000,
  "params":{"durationMs":3000}
}'

# 2. A CPU-heavy prime computation
curl -s -X POST "$BASE" -H 'Content-Type: application/json' -d '{
  "name":"count-primes",
  "type":"COMPUTE",
  "priority":"HIGH",
  "params":{"limit":20000000}
}'

# 3. A flaky job that exercises automatic retries (failRate=90, maxRetries=3)
curl -s -X POST "$BASE" -H 'Content-Type: application/json' -d '{
  "name":"flaky-job",
  "type":"DELAY",
  "priority":"NORMAL",
  "maxRetries":3,
  "failRate":90,
  "params":{"durationMs":800}
}'

# 4. CRITICAL priority - watch it jump the queue
curl -s -X POST "$BASE" -H 'Content-Type: application/json' -d '{
  "name":"urgent",
  "type":"FETCH",
  "priority":"CRITICAL",
  "params":{"url":"https://example.com/payments"}
}'

# 5. Producer-consumer burst demo: 20 synthetic tasks stream in
curl -s -X POST "http://localhost:8080/api/tasks/burst?count=20"

# 6. In another terminal, watch the live event stream
curl -N http://localhost:8080/api/events
```

Tasks can be cancelled (`DELETE /api/tasks/{id}`) while PENDING/QUEUED/RUNNING
and retried (`POST /api/tasks/{id}/retry`) when FAILED/TIMED_OUT.

---

## 📡 REST API reference

| Method | Path | Description |
| --- | --- | --- |
| `POST` | `/api/tasks` | Submit a task (JSON body below) → `201` |
| `GET` | `/api/tasks?status=RUNNING` | List tasks (newest first), optional filter |
| `GET` | `/api/tasks/{id}` | One task incl. full event log |
| `DELETE` | `/api/tasks/{id}` | Cancel (PENDING/QUEUED/RUNNING only) |
| `POST` | `/api/tasks/{id}/retry` | Requeue a FAILED / TIMED_OUT task |
| `POST` | `/api/tasks/burst?count=N` | Submit N synthetic tasks (demo) |
| `GET` | `/api/stats` | Live counters + latency percentiles |
| `GET` | `/api/events` | SSE stream of `task` events |
| `GET` | `/api/health` | Health check |

### `POST /api/tasks` request body

```json
{
  "name": "my-job",                 // required, ≤ 80 chars
  "type": "DELAY",                  // DELAY | COMPUTE | FETCH | BUILD_REPORT
  "priority": "NORMAL",             // LOW | NORMAL | HIGH | CRITICAL
  "delayMillis": 0,                 // ms before execution starts
  "timeoutMillis": 60000,           // execution timeout
  "maxRetries": 0,                  // automatic retries after failure
  "failRate": 0,                    // 0-100 % chance to fail (demo only)
  "params": { "durationMs": 5000 }  // type-specific parameters
}
```

All fields except `name` are optional. Type-specific params:

- `DELAY` → `params.durationMs` (100 – 120000)
- `COMPUTE` → `params.limit` (10000 – 50000000)
- `FETCH` → `params.url`
- `BUILD_REPORT` → *(none)*

### Error contract

All errors use `{ "timestamp", "status", "error", "message", "fields"? }`.
`404` = unknown task · `400` = validation / bad state · `500` = internal.
## ⚙️ Configuration (`application.yml`)

| Key | Default | Meaning |
| --- | --- | --- |
| `scheduler.workers` | `4` | Executor pool size (running jobs) |
| `scheduler.queue-capacity` | `2048` | Priority queue capacity hint |
| `scheduler.default-timeout-ms` | `60000` | Execution timeout fallback |
| `scheduler.max-retries` | `2` | Retry fallback for the UI form |
| `scheduler.heartbeat-ms` | `15000` | SSE heartbeat interval |

## 🧪 Running the tests

```bash
cd backend
mvn test
```

`TaskSchedulerIntegrationTest` boots the real Spring context and asserts:
completion of an async task, priority preemption in the queue, retry exhaustion,
and cancellation of a pending task.

---

## 📚 Documentation

- [`docs/DESIGN.md`](docs/DESIGN.md) — architecture, threading model, algorithm,
  and a full **advanced-Java feature catalogue** with code references.
- [`docs/PROJECT_REPORT.md`](docs/PROJECT_REPORT.md) — fill-in-the-sections
  submission report for a 2nd-year project.

---

## 🔍 Troubleshooting

| Symptom | Remedy |
| --- | --- |
| `java: command not found` | Install JDK 17+, or run Maven with `JAVA_HOME` set |
| Port 8080 already in use | `server.port: 8081` in `application.yml` |
| `npm: command not found` | Install Node 18+ |
| Dashboard shows *connecting…* | Backend not running, or Vite proxy not used (`npm run dev`) |
| CORS errors (rare) | `CorsConfig.java` already allows `localhost:5173` |

---

## 🧑‍🎓 Learning goals addressed

1. **Java concurrency** — executors, blocking queues, thread-safe collections,
   atomic counters, `volatile` semantics, cooperative cancellation.
2. **REST + reactive frontends** — Spring MVC JSON API, CORS, SSE.
3. **Functional Java** — records, switch expressions, streams/parallel streams.
4. **Engineering quality** — DTO and exception contracts, config binding, tests,
   graceful shutdown, structured logs.
> RUNNING (progress) → COMPLETED in real time.