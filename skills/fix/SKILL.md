---
name: sdd.fix
description: "Stage 6 of the agentic-sdd pipeline. Addresses a verify FAIL under a 3-attempt circuit breaker. Status implemented (re-verify) or failed (breaker tripped)."
---

# /sdd.fix [feature-id] [--reset]

**Precondition:** `specs/{feature-id}/status.json` stage must be `failed` or
`fixing`. If the precondition fails, tell the user the current stage and stop.

`--reset` clears `fixAttempts` back to 0 — use only when the requester has
explicitly decided to give the circuit breaker a fresh 3 attempts (e.g. after
a plan change), never automatically.

## What to do when invoked

1. Read `verification-report.md`'s failing items and `status.json`'s current
   `fixAttempts`.

2. **Circuit breaker check (hard rule):** if `fixAttempts >= 3` and `--reset`
   was not passed, stop immediately. Tell the user: the breaker has tripped,
   summarize all prior `fix-log.md` attempts, and recommend `/sdd.plan
   --force` instead of another fix attempt. Do not spawn `sdd-fixer`.

3. Otherwise: increment `fixAttempts`, set `status.json` stage to `fixing`,
   and spawn `sdd-fixer` with `verification-report.md`'s failing items, the
   relevant diff, spec, and plan. It writes
   `specs/{feature-id}/fix-log.md` (append this attempt, don't overwrite
   prior attempts) using `templates/fix-report-template.md`.

4. If `sdd-fixer`'s response is `SPEC_DRIFT:` — handle exactly like
   `/sdd.implement` step 2d: route to plan or spec revision, don't count it
   against `fixAttempts` (a plan error isn't a failed fix attempt).

5. Otherwise: set `status.json` stage to `implemented`.

6. **Wiki mirror (optional, degrades gracefully):** create/update a "Fix Log"
   child page (append this attempt). Skip in one line if not connected.

7. Report status and the next command: `/sdd.verify {feature-id}`.
