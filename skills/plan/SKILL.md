---
name: sdd.plan
description: "Stage 2 of the agentic-sdd pipeline. Turns an approved spec into 2-plan.md and 3-tasks.md, status planned."
---

# /sdd.plan [feature-id] [--force]

**Precondition:** the stage in `specs/{feature-id}/status.json` must be `reviewed`.
With `--force`, any stage is allowed (use it to plan again after
feedback). If the precondition fails and `--force` is not given, tell the user the
current stage and stop.

## What to do when invoked

1. Read `1-spec.md` and `specs/constitution.md` in full.

2. **Knowledge grounding (optional, degrades gracefully):** use the same bundled-
   engine call as `/sdd.require` step 3. If this run follows `/sdd.require` immediately
   in the same session, reuse the same result. If not, run the search
   again. If the engine is not built or Java is not available, skip this step and say so in one line.

3. Spawn `sdd-architect` with the spec, the constitution, and any knowledge
   context. Tell it to write `2-plan.md`
   (`templates/plan-template.md`) and `3-tasks.md`
   (`templates/tasks-template.md`). Each task gets a suggested `(role)` tag.
   A reasonable guess against the 12-role table in
   `skills/implement/SKILL.md` is enough. The deterministic
   re-scoring step of that stage corrects the tag later, so do not spend long on it here.

4. **Finalize task owners (deterministic, no subagent):** for each numbered
   task in `3-tasks.md`, score the text of the task against the exact same 12-role
   trigger table that `skills/implement/SKILL.md` defines (same signals, same
   tie-break architect > seniorDev > specialist, same default seniorDev). If
   the result is different from the tag that the architect suggested, correct the tag with an
   Edit. Owners are locked here. You must not derive them again later.

5. Show `2-plan.md` and `3-tasks.md` to the user. Use AskUserQuestion: "Approve —
   continue to Analyze" / "Revise — describe changes" / "Cancel". For Revise,
   spawn `sdd-architect` again with the changes appended, then go back to step 4. For Cancel,
   stop and leave the plan on disk as a draft.

6. If the user approves: set the `status.json` stage to `planned` and add an entry to `history`.

7. **Wiki mirror (optional, degrades gracefully):** make or update a "Plan &
   Tasks" child page under the ticket page of the feature. Put the content of both files on it.
   If the wiki is not connected, skip this step and say so in one line.

8. Report the plan and tasks paths and the status, and the next command:
   `/sdd.analyze {feature-id}`.
