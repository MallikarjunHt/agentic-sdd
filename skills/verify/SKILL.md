---
name: sdd.verify
description: "Stage 5 of the agentic-sdd pipeline. Runs constitution, quality/security, and test checks plus an adversarial pass, then proposes a CLAUDE.md sync on PASS. Status verified or failed."
---

# /sdd.verify [feature-id] [--force]

**Precondition:** `specs/{feature-id}/status.json` stage must be
`implemented` or `fixing` (or any stage with `--force`). If the precondition
fails without `--force`, tell the user the current stage and stop.

## What to do when invoked

1. Spawn, in order (each reads the actual diff, not just the plan):
   `sdd-verify-constitution`, `sdd-verify-quality-security`,
   `sdd-verify-tests` — each writes its section of `verification-report.md`
   (`templates/verification-report-template.md`) and ends with its own
   `..._CHECK: pass|fail` marker.

2. Spawn `sdd-devil-advocate` with the three completed sections plus the full
   diff, spec, and plan. It writes the "Devil's advocate pass" section and
   ends with `DEVIL_ADVOCATE: clear|concerns`.

3. Determine the overall verdict:
   - **PASS**: all three checks pass and devil's advocate is clear.
   - **CONCERNS**: all three checks pass but devil's advocate raised concerns.
   - **FAIL**: any of the three checks failed.

4. **On PASS:** propose a `CLAUDE.md` diff for the target repo (what was
   learned/changed that future sessions should know) in the "CLAUDE.md sync
   proposal" section. Show it to the user via AskUserQuestion: "Commit this
   diff on the feature branch" / "Skip the sync" / "Let me edit it first".
   Never commit it silently — it goes through the same PR review as the
   rest of the change. Update `status.json` stage to `verified`.

5. **On CONCERNS:** show the devil's advocate findings via AskUserQuestion:
   "Accept as-is" / "Address specific concerns". If addressing, append each
   accepted concern as a new task to `3-tasks.md`, set `status.json` stage
   to `partial`, and tell the user to re-run `/sdd.implement` then
   `/sdd.verify` again for just those tasks.

6. **On FAIL:** update `status.json` stage to `failed`. Report exactly which
   check(s) failed and why.

7. **Wiki mirror (optional, degrades gracefully):** create/update a
   "Verification Report" child page. Skip in one line if not connected.

8. Report the verdict and the next command: `/sdd.fix {feature-id}` on FAIL,
   or report completion on PASS.
