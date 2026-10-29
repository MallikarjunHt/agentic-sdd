---
name: sdd.status
description: "Read-only. Shows the current pipeline stage and artifact state for a feature, or lists all features if none is given."
---

# /sdd.status [feature-id]

This command is read-only. It must not write anything. It must not spawn an
agent.

## What to do when invoked

- With `feature-id`: read `specs/{feature-id}/status.json`. List which
  artifacts exist on disk (`1-spec.md` through `fix-log.md`,
  `verification-report.md`). Report the stage, `fixAttempts`, and the next
  command to run (see the stage-transition table in `README.md`).
- Without an argument: list every `specs/*/status.json`. Use one line for
  each: feature-id, stage, and the last-updated timestamp from `history`.
- If `wiki.enabled` is set in the config, make one HEAD/search-style MCP call.
  Note whether the wiki mirror pages exist for this feature. This is for
  information only. The status report must not depend on it. If the wiki tool
  is not connected, say so in one line and continue with the local status
  only.
