#!/usr/bin/env python3
"""Append Appendix A (backend source) to PROJECT_REPORT_FULL.md."""
from pathlib import Path
ROOT = Path("/Users/mishrapawas/Documents/class/2nd Year project")
OUT = ROOT / "docs" / "PROJECT_REPORT_FULL.md"
FILES = [
 ("backend/src/main/java/com/taskscheduler/AsyncTaskSchedulerApp.java", "java"),
 ("backend/src/main/java/com/taskscheduler/common/TaskStatus.java", "java"),
 ("backend/src/main/java/com/taskscheduler/common/TaskPriority.java", "java"),
 ("backend/src/main/java/com/taskscheduler/common/TaskType.java", "java"),
 ("backend/src/main/java/com/taskscheduler/model/TaskRecord.java", "java"),
 ("backend/src/main/java/com/taskscheduler/model/TaskSubmitRequest.java", "java"),
 ("backend/src/main/java/com/taskscheduler/model/TaskView.java", "java"),
 ("backend/src/main/java/com/taskscheduler/model/StatsView.java", "java"),
 ("backend/src/main/java/com/taskscheduler/scheduler/TaskSchedulerService.java", "java"),
 ("backend/src/main/java/com/taskscheduler/scheduler/PriorityTaskQueue.java", "java"),
 ("backend/src/main/java/com/taskscheduler/scheduler/QueuedTask.java", "java"),
 ("backend/src/main/java/com/taskscheduler/scheduler/DemoJobFactory.java", "java"),
 ("backend/src/main/java/com/taskscheduler/scheduler/SchedulerMetrics.java", "java"),
 ("backend/src/main/java/com/taskscheduler/scheduler/SchedulerProperties.java", "java"),
 ("backend/src/main/java/com/taskscheduler/scheduler/SchedulerException.java", "java"),
 ("backend/src/main/java/com/taskscheduler/scheduler/TaskNotFoundException.java", "java"),
 ("backend/src/main/java/com/taskscheduler/scheduler/JobFailureException.java", "java"),
 ("backend/src/main/java/com/taskscheduler/scheduler/JobCancelledException.java", "java"),
 ("backend/src/main/java/com/taskscheduler/web/TaskController.java", "java"),
 ("backend/src/main/java/com/taskscheduler/web/DashboardController.java", "java"),
 ("backend/src/main/java/com/taskscheduler/web/GlobalExceptionHandler.java", "java"),
 ("backend/src/main/java/com/taskscheduler/config/CorsConfig.java", "java"),
 ("backend/src/main/resources/application.yml", "yaml"),
 ("backend/pom.xml", "xml"),
 ("backend/src/test/java/com/taskscheduler/TaskSchedulerIntegrationTest.java", "java"),
 ("backend/src/test/java/com/taskscheduler/scheduler/PriorityTaskQueueTest.java", "java"),
]
lines = ["", "---", "",
 "## Appendix A — Complete Backend Source Code", "",
 "Every backend file is reproduced verbatim below, in package order.", ""]
for rel, lang in FILES:
    p = ROOT / rel
    src = p.read_text(encoding="utf-8") if p.exists() else f"<!-- MISSING: {rel} -->"
    lines += [f"### A.{FILES.index((rel, lang)) + 1} `{rel}`", "", f"```{lang}", src.rstrip(), "```", ""]
with open(OUT, "a", encoding="utf-8") as f:
    f.write("\n".join(lines))
print("Appendix A appended:", len(lines), "lines")
