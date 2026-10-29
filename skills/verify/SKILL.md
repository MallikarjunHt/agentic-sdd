---
name: sdd.verify
description: "Stage 5 of the agentic-sdd pipeline. Runs constitution, quality/security, and test checks plus an adversarial pass, then proposes a CLAUDE.md sync on PASS. Status verified or failed."
---

# /sdd.verify [feature-id] [--force]

**Precondition:** `specs/{feature-id}/status.json` stage must be
`implemented` or `fixing` (or any stage with `--force`). If the precondition
fails without `--force`, tell the user the current stage and stop.

## What to do when invoked

1. Spawn these agents in this order: `sdd-verify-constitution`,
   `sdd-verify-quality-security`, `sdd-verify-tests`. Each agent reads the
   actual diff, not only the plan. Each agent writes its section of
   `verification-report.md` (`templates/verification-report-template.md`).
   Each agent ends with its own `..._CHECK: pass|fail` marker.

2. Spawn `sdd-devil-advocate` with the three completed sections, the full
   diff, the spec, and the plan. The agent writes the "Devil's advocate pass"
   section and ends with `DEVIL_ADVOCATE: clear|concerns`.

3. Find the overall verdict:
   - **PASS**: all three checks pass and devil's advocate is clear.
   - **CONCERNS**: all three checks pass but devil's advocate raised concerns.
   - **FAIL**: any of the three checks failed.

4. **On PASS:** write a proposed `CLAUDE.md` diff for the target repo in the
   "CLAUDE.md sync proposal" section. The diff has what future sessions must
   know from this change. Show it to the user with AskUserQuestion: "Commit
   this diff on the feature branch" / "Skip the sync" / "Let me edit it
   first". Never commit it silently. It must go through the same PR review as
   the rest of the change. Update `status.json` stage to `verified`.

5. **On CONCERNS:** show the devil's advocate findings with AskUserQuestion:
   "Accept as-is" / "Address specific concerns". To address concerns, append
   each accepted concern as a new task to `3-tasks.md` and set `status.json`
   stage to `partial`. Tell the user to run `/sdd.implement` and then
   `/sdd.verify` again for only those tasks.

6. **On FAIL:** update `status.json` stage to `failed`. Report which checks
   failed and why.

7. **Wiki mirror (optional, degrades gracefully):** make or update a
   "Verification Report" child page. If the wiki is not connected, skip this
   step and say so in one line.

8. Report the verdict and the next command. On FAIL, the next command is
   `/sdd.fix {feature-id}`. On PASS, report completion.
