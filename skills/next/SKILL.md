---
name: sdd.next
description: "Dispatches to whichever /sdd.* command is correct for a feature's current stage, so the user doesn't have to remember the stage-to-command mapping."
---

# /sdd.next [feature-id]

## What to do when invoked

1. Read `specs/{feature-id}/status.json`'s `stage`.
2. Dispatch via this exact mapping (same table as `README.md`):

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

3. Tell the user which command you're dispatching to before running it, so
   the mapping stays transparent rather than feeling automatic/opaque.
