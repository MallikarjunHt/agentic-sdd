---
name: sdd.status
description: "Read-only. Shows the current pipeline stage and artifact state for a feature, or lists all features if none is given."
---

# /sdd.status [feature-id]

Read-only — never writes anything, never spawns an agent.

## What to do when invoked

- With `feature-id`: read `specs/{feature-id}/status.json` and list which
  artifacts exist on disk (`1-spec.md` through `fix-log.md`,
  `verification-report.md`). Report stage, `fixAttempts`, and which command
  to run next (see the stage-transition table in `README.md`).
- Without an arg: list every `specs/*/status.json`, one line each —
  feature-id, stage, last-updated timestamp from `history`.
- If `wiki.enabled` in config, note whether the wiki mirror pages exist for
  this feature (one HEAD/search-style MCP call) — but this is informational
  only, never required for the status report to succeed. If the wiki tool
  isn't connected, say so in one line and continue with local status only.
