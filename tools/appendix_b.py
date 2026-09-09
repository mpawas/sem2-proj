#!/usr/bin/env python3
"""Append Appendix B (frontend source) to PROJECT_REPORT_FULL.md."""
from pathlib import Path
ROOT = Path("/Users/mishrapawas/Documents/class/2nd Year project")
OUT = ROOT / "docs" / "PROJECT_REPORT_FULL.md"
FILES = [
 ("frontend/index.html", "html"),
 ("frontend/package.json", "json"),
 ("frontend/vite.config.js", "js"),
 ("frontend/src/main.jsx", "jsx"),
 ("frontend/src/api.js", "js"),
 ("frontend/src/status.js", "js"),
 ("frontend/src/components/TaskForm.jsx", "jsx"),
 ("frontend/src/components/TaskTable.jsx", "jsx"),
 ("frontend/src/components/TaskDetailModal.jsx", "jsx"),
 ("frontend/src/components/StatsBar.jsx", "jsx"),
 ("frontend/src/components/StatusBadge.jsx", "jsx"),
 ("frontend/src/styles.css", "css"),
]
lines = ["", "---", "",
 "## Appendix B — Complete Frontend Source Code", "",
 "Every frontend file is reproduced verbatim below.", ""]
for i, (rel, lang) in enumerate(FILES, start=1):
    p = ROOT / rel
    src = p.read_text(encoding="utf-8") if p.exists() else f"<!-- MISSING: {rel} -->"
    lines += [f"### B.{i} `{rel}`", "", f"```{lang}", src.rstrip(), "```", ""]
with open(OUT, "a", encoding="utf-8") as f:
    f.write("\n".join(lines))
print("Appendix B appended:", len(lines), "lines")
