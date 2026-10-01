---
name: sdd.plan
description: "Stage 2 of the agentic-sdd pipeline. Turns an approved spec into 2-plan.md and 3-tasks.md, status planned."
---

# /sdd.plan [feature-id] [--force]

**Precondition:** `specs/{feature-id}/status.json` stage must be `reviewed`
(or any stage, if `--force` is passed — used to redo planning after
feedback). If the precondition fails without `--force`, tell the user the
current stage and stop.

## What to do when invoked

1. Read `1-spec.md` and `specs/constitution.md` in full.

2. **Knowledge grounding (optional, degrades gracefully):** same pattern as
   `/sdd.require` step 3 — reuse the same query/result if this run is
   immediately after `/sdd.require` in the same session; otherwise re-query.
   Skip in one line if the tool isn't connected.

3. Spawn `sdd-architect` with the spec, the constitution, any knowledge
   context, and an instruction to write `2-plan.md`
   (`templates/plan-template.md`) and `3-tasks.md`
   (`templates/tasks-template.md`). Each task gets a suggested `(role)` tag —
   a reasonable guess against the 12-role table in
   `skills/implement/SKILL.md` is enough; that stage's deterministic
   re-scoring step corrects it later, so don't over-deliberate here.

4. **Finalize task owners (deterministic, no subagent):** for each numbered
   task in `3-tasks.md`, score its text against the exact same 12-role
   trigger table `skills/implement/SKILL.md` defines (same signals, same
   tie-break architect > seniorDev > specialist, same default seniorDev). If
   this disagrees with the architect's suggested tag, correct it with an
   Edit. Owners are locked here and never re-derived later.

5. Show `2-plan.md` and `3-tasks.md` to the user. AskUserQuestion: "Approve —
   continue to Analyze" / "Revise — describe changes" / "Cancel". Revise
   re-spawns `sdd-architect` with the delta appended, back to step 4. Cancel
   stops, leaving the plan on disk as a draft.

6. On approve: update `status.json` stage to `planned`, append to `history`.

7. **Wiki mirror (optional, degrades gracefully):** create/update a "Plan &
   Tasks" child page under the feature's ticket page with both files'
   content. Skip in one line if not connected.

8. Report the plan/tasks paths and status, and the next command:
   `/sdd.analyze {feature-id}`.
