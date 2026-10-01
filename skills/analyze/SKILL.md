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
   the target repo's current state (it reads the repo directly — this is not
   a knowledge-base-only check, the point is comparing the plan against
   reality). It writes `4-gap-report.md` and ends with `ANALYSIS: analyzed`
   or `ANALYSIS: gaps-found`.

2. Show `4-gap-report.md` to the user.

3. If `ANALYSIS: gaps-found`:
   - Update `status.json` stage to `gaps-found`.
   - AskUserQuestion per open CRITICAL finding (or batched, if several share
     a resolution): "Update the plan" / "Descope (update the spec's Out of
     scope)" / "Accept as a known limitation". Route "Update the plan" back
     to `/sdd.plan --force`; route "Descope" back to `/sdd.require --force`
     with the descope text; "Accept" requires an explicit note of who
     accepted it, recorded in the gap report's Resolution section.
   - Once every CRITICAL finding is resolved, re-run this stage.

4. If `ANALYSIS: analyzed`: update `status.json` stage to `analyzed`.

5. **Wiki mirror (optional, degrades gracefully):** create/update a "Gap
   Report" child page. Skip in one line if not connected.

6. Report the gap-report path and status, and the next command:
   `/sdd.implement {feature-id}`.
