---
name: sdd.next
description: "Dispatches to whichever /sdd.* command is correct for a feature's current stage, so the user does not need to remember the stage-to-command mapping."
---

# /sdd.next [feature-id]

## What to do when invoked

1. Read the `stage` in `specs/{feature-id}/status.json`.
2. Use this exact mapping (it is the same table as in `README.md`):

| stage | runs |
|---|---|
| (no status.json yet) | `/sdd.require {feature-id}` |
| `reviewed` | `/sdd.plan {feature-id}` |
| `planned` | `/sdd.analyze {feature-id}` |
| `gaps-found` | `/sdd.analyze {feature-id}` (resume gap resolution) |
| `analyzed` | `/sdd.implement {feature-id}` |
| `partial` | `/sdd.implement {feature-id}` (resume) |
| `implemented` | `/sdd.verify {feature-id}` |
| `fixing` | `/sdd.fix {feature-id}` (resume) |
| `failed` | `/sdd.fix {feature-id}` |
| `verified` | nothing to do — report completion |

3. Tell the user which command you will run before you run it. This keeps the
   mapping clear to the user.
