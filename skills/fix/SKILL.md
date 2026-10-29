---
name: sdd.fix
description: "Stage 6 of the agentic-sdd pipeline. Addresses a verify FAIL under a 3-attempt circuit breaker. Status implemented (re-verify) or failed (breaker tripped)."
---

# /sdd.fix [feature-id] [--reset]

**Precondition:** `specs/{feature-id}/status.json` stage must be `failed` or
`fixing`. If the precondition fails, tell the user the current stage and stop.

`--reset` sets `fixAttempts` back to 0. Use it only when the requester has
decided to give the circuit breaker 3 new attempts (for example, after a plan
change). Never use it automatically.

## What to do when invoked

1. Read the failing items in `verification-report.md`. Read the current
   `fixAttempts` in `status.json`.

2. **Circuit breaker check (hard rule):** if `fixAttempts >= 3` and `--reset`
   was not passed, stop at once. Do not spawn `sdd-fixer`. Tell the user that
   the breaker has tripped. Summarize all prior attempts in `fix-log.md`.
   Recommend `/sdd.plan --force` instead of another fix attempt.

3. Otherwise, add 1 to `fixAttempts` and set `status.json` stage to `fixing`.
   Then spawn `sdd-fixer` with the failing items from
   `verification-report.md`, the relevant diff, the spec, and the plan. The
   agent writes `specs/{feature-id}/fix-log.md` with
   `templates/fix-report-template.md`. It must append this attempt and must
   not overwrite prior attempts.

4. If the response of `sdd-fixer` is `SPEC_DRIFT:`, do the same as
   `/sdd.implement` step 2d. Go back to a plan or spec revision. Do not count
   it as a `fixAttempts` attempt, because a plan error is not a failed fix
   attempt.

5. Otherwise, set `status.json` stage to `implemented`.

6. **Wiki mirror (optional, degrades gracefully):** make or update a "Fix Log"
   child page and append this attempt. If the wiki is not connected, skip this
   step and say so in one line.

7. Report the status. Then show the next command: `/sdd.verify {feature-id}`.
