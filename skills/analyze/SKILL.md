---
name: sdd.analyze
description: "Stage 3 of the agentic-sdd pipeline. Gap-checks the plan against the spec and the real codebase before any code is written. Status analyzed or gaps-found."
---

# /sdd.analyze [feature-id] [--force]

**Precondition:** `specs/{feature-id}/status.json` stage must be `planned` or
`gaps-found` (or any stage with `--force`). If the precondition fails without
`--force`, tell the user the current stage and stop.

## What to do when invoked

1. Spawn `sdd-gap-analyst` with `1-spec.md`, `2-plan.md`, `3-tasks.md`, and
   the current state of the target repo. The agent reads the repo directly.
   This is not a knowledge-base-only check: the plan must be compared with
   the real code. The agent writes `4-gap-report.md` and ends with
   `ANALYSIS: analyzed` or `ANALYSIS: gaps-found`.

2. Show `4-gap-report.md` to the user.

3. If `ANALYSIS: gaps-found`:
   - Update `status.json` stage to `gaps-found`.
   - Use AskUserQuestion for each open CRITICAL finding. You can batch
     findings that share a resolution. The options are: "Update the plan" /
     "Descope (update the spec's Out of scope)" / "Accept as a known
     limitation".
   - For "Update the plan", go back to `/sdd.plan --force`.
   - For "Descope", go back to `/sdd.require --force` with the descope text.
   - "Accept" needs a note of who accepted it. Record the note in the
     Resolution section of the gap report.
   - When every CRITICAL finding is resolved, run this stage again.

4. If `ANALYSIS: analyzed`: update `status.json` stage to `analyzed`.

5. **Wiki mirror (optional, degrades gracefully):** make or update a "Gap
   Report" child page. If the wiki is not connected, skip this step and say
   so in one line.

6. Report the gap-report path and the status. Then show the next command:
   `/sdd.implement {feature-id}`.
