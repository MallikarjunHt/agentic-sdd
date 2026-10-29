# Session discipline for a real /sdd.* run

These notes show how to drive a feature through this pipeline. Each
`skills/*/SKILL.md` already covers the stage-by-stage mechanics.

## One feature per session is the sane default

Each stage starts one or more subagents and reads several files again. If you
run the stages of two features in one conversation, you can answer an
AskUserQuestion gate for the wrong feature. To work on two features in
parallel, use two separate sessions (or two worktrees). Do not mix `/sdd.*`
calls for different feature-ids in one session.

## Approval gates are real stops

Every gate is an `AskUserQuestion` call. The gates are the spec approval in
`require`, the plan approval in `plan`, and the CLAUDE.md-sync decision in
`verify`. A gate is not a rhetorical "does this look right?" If a gate has no
answer, the pipeline is paused, not stuck. Nothing continues until you answer
it.

## If a session is interrupted mid-stage

The `stage` field in `status.json` shows the *last completed* stage, not a
stage in progress. A stage changes `stage` only after it fully finishes
(including the human gate, where one applies). After an interruption:

1. Run `/sdd.status {feature-id}`. It shows the last completed stage and the
   artifacts on disk.
2. Run `/sdd.next {feature-id}`. It resumes from the correct next command.
   `implement` and `fix` resume mid-checklist (`partial`/`fixing`). They do
   not restart from task 1 or attempt 1.

## When a stage disagrees with reality

`SPEC_DRIFT:` (from any implement/fix-stage specialist) and `gaps-found`
(from `/sdd.analyze`) both stop the pipeline and ask you. They prevent a
subagent from quietly working around a contradiction. Treat each one as a real
decision point, not a glitch. If you retry the same stage and do not resolve
the flagged problem, it will produce the same drift again.

## The fix circuit breaker is deliberate, not a bug

If `/sdd.fix` reports that the breaker tripped (3 attempts, no clean
re-check), the right action is almost always `/sdd.plan --force`. The
implementation is probably not the problem. The plan's approach to this
failure is the problem. Do not use `--reset` to try a 4th fix attempt, unless
you changed the approach between attempts.
