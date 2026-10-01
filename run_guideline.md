# Session discipline for a real /sdd.* run

Practical notes for actually driving a feature through this pipeline, as
opposed to the stage-by-stage mechanics already covered in each
`skills/*/SKILL.md`.

## One feature per session is the sane default

Each stage spawns one or more subagents and re-reads several files; running
two features' worth of stages interleaved in one conversation makes it easy
to answer an AskUserQuestion gate for the wrong feature. If you need to work
two features in parallel, use two separate sessions (or two worktrees) rather
than interleaving `/sdd.*` calls for different feature-ids in one.

## Approval gates are real stops

Every gate (`require`'s spec approval, `plan`'s plan approval, `verify`'s
CLAUDE.md-sync decision) is an `AskUserQuestion` call, not a rhetorical
"does this look right?" Treat an unanswered gate as the pipeline being
paused, not stuck — nothing proceeds until it's answered.

## If a session is interrupted mid-stage

`status.json`'s `stage` reflects the *last completed* stage, not an
in-progress one — a stage only advances `stage` after it fully finishes
(including, where relevant, the human gate). So after an interruption:

1. Run `/sdd.status {feature-id}` to see the last completed stage and which
   artifacts exist on disk.
2. Run `/sdd.next {feature-id}` to resume from the correct next command —
   `implement` and `fix` resume mid-checklist (`partial`/`fixing`) rather
   than restarting from task 1 / attempt 1.

## When a stage disagrees with reality

`SPEC_DRIFT:` (from any implement/fix-stage specialist) and `gaps-found`
(from `/sdd.analyze`) are both designed to stop the pipeline and ask you,
rather than let a subagent quietly route around a contradiction. Treat either
as a genuine decision point, not a glitch to retry past — retrying the exact
same stage without resolving what it flagged will just produce the same
drift again.

## The fix circuit breaker is deliberate, not a bug

If `/sdd.fix` reports the breaker tripped (3 attempts, no clean re-check), the
right move is almost always `/sdd.plan --force` — the implementation
probably isn't the problem, the plan's approach to this specific failure is.
Resist the urge to `--reset` and try a 4th fix attempt unless you've actually
changed something about the approach between attempts.
