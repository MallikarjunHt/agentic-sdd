# Agentic-SDD

Agentic, spec-driven development. You give one feature request. You get a
spec, a plan, a gap analysis, code from specialist agents, and a verification
report. A status file on disk gates every stage, not the conversation state.
**Fully standalone**: this repo has no dependency, import, submodule, or
hardcoded path to any other repo or plugin. It installs and runs on its own.

## Design principle: the knowledge base is bundled; only the wiki mirror is MCP-only

The knowledge-base engine is in `knowledge-engine/`. It is a renamed,
standalone fork of a Lucene-based BM25/vector search tool (MIT licensed, see
its own README). This repo contains its **source**, not a binary.
`/sdd.init` builds it once on the local machine with Java 21+ and Maven. Then
every later stage runs the jar directly with `java -jar`. No MCP server and no
other repo is in the loop. It keeps working if you uninstall every other
plugin and MCP server on the machine.

No embedding model ships with it. A model would add about 90MB of binary
weight to a plugin that is otherwise prompts and templates. By default, it
indexes and searches with BM25 only. Vector search is an optional upgrade. Put
`model.onnx` and `vocab.txt` in `.agentic-sdd/knowledge-engine-models/` (see
`knowledge-engine/README.md` for a known-good source). Nothing else needs a
new configuration.

The **wiki mirror** is the one remaining optional *external* dependency. It is
a real external system (Confluence or similar). The plugin checks it at
runtime with `mcp__confluence__confluence_createContent` and
`confluence_updateContent`. If it is not connected, every stage that calls it
continues and says so in one line. The local artifacts under
`specs/{feature-id}/` are always the real source of truth. Run `/sdd.tooling`
at any time to see what is built and connected. In the stages, you see this as
two steps:

- A "Knowledge grounding (optional, degrades gracefully)" step in
  `require` and `plan`.
- A "Wiki mirror (optional, degrades gracefully)" step in every stage that
  writes an artifact.

## Quick start

```bash
# 1 — register this repo as a Claude Code marketplace (once per machine)
claude plugin marketplace add /path/to/agentic-sdd

# 2 — install into the target repo
cd /path/to/target-repo
claude plugin install agentic-sdd@agentic-sdd --scope project

# 3 — restart the session, then run one-time setup
/sdd.init

# 4 — drive a feature end to end
/sdd.require my-feature-id
```

> [!IMPORTANT]
> Plugins load at session start. After you install, you must restart or
> reload Claude Code. If you do not, the `sdd.*` commands and `sdd-*` agents
> do not show.

## The pipeline

| # | Command | Runs only if stage is | Writes | Wiki mirror | Stage after |
|---|---|---|---|---|---|
| — | `/sdd.init` | — (one-time) | `.agentic-sdd/config.yaml`, `specs/constitution.md` | verifies parent page reachable | — |
| 1 | `/sdd.require [feature-id]` | — (entry point) | `1-spec.md` | ticket + Spec child page | `reviewed` |
| 2 | `/sdd.plan [feature-id] [--force]` | `reviewed` (any with `--force`) | `2-plan.md`, `3-tasks.md` | Plan & Tasks child page | `planned` |
| 3 | `/sdd.analyze [feature-id] [--force]` | `planned` or `gaps-found` (any with `--force`) | `4-gap-report.md` | Gap Report child page | `analyzed` or `gaps-found` |
| 4 | `/sdd.implement [feature-id]` | `analyzed` or `partial` | code, tests | Implementation Summary child page | `implemented` or `partial` |
| 5 | `/sdd.verify [feature-id] [--force]` | `implemented` or `fixing` | `verification-report.md`, a proposed `CLAUDE.md` diff on PASS | Verification Report child page | `verified` or `failed` |
| 6 | `/sdd.fix [feature-id] [--reset]` | `failed` or `fixing` | `fix-log.md` | Fix Log child page | `implemented` or `failed` |
| — | `/sdd.status [feature-id]` | any | — (read-only) | — | — |
| — | `/sdd.next [feature-id]` | any | dispatches the correct next stage | — | — |
| — | `/sdd.tooling` | any | — (read-only) | — | — |

`specs/{feature-id}/status.json` gates every stage after `require`. The
pipeline cannot run out of order. A crashed run resumes and does not restart.
See [`run_guideline.md`](run_guideline.md) for session discipline on a real
run.

## What's here beyond a plain spec->plan->code->review loop

- **A dedicated gap-analysis stage** (`sdd-gap-analyst`, `/sdd.analyze`). It
  checks the plan against the acceptance criteria of the spec *and* the real
  current codebase, before any code is written. CRITICAL findings block
  progress until you resolve them or explicitly descope or accept them.
- **A three-way verify split plus an adversarial pass**: three separate agents
  check constitution conformance, quality/security, and test adequacy. No
  single check must cover everything. Then `sdd-devil-advocate` looks for
  what a checklist-shaped review cannot find (concurrency, migration/rollback
  risk, backward compatibility).
- **A fix stage with a real circuit breaker**: `sdd-fixer` has 3 attempts for
  each verify FAIL. After that, it must stop and recommend a plan change. It
  does not loop without end.
- **A `CLAUDE.md` sync, proposed once, only on verify PASS**: the plugin
  commits it to the same feature branch. The normal PR reviews it. It is never
  a silent commit.
- **A profile/constitution system**: one plugin, any number of target repos.
  Each repo uses its own real conventions (`specs/constitution.md`), not a
  generic textbook standard. If the detected config of the repo and this file
  disagree, the config of the repo wins.
- **A 12-role specialist roster with keyword-based routing** (see
  `skills/implement/SKILL.md`) and a model tier for each task by complexity
  (haiku/sonnet/opus). It covers more of the stack than one general-purpose
  "builder" agent, and it is cost-aware.

## Known gaps

These gaps are stated plainly. Do not plan around a component that does not
exist yet.

| Gap | Impact |
|---|---|
| **No automated validation of this plugin itself.** | A compiler or test suite checks nothing here. To prove a change, run the affected `/sdd.*` command against a real feature. See [`CLAUDE.md`](CLAUDE.md). |
| **No cross-repo coordination.** | Each install targets one repo through its own `.agentic-sdd/config.yaml`. A feature across two repos needs two separate runs, which you coordinate by hand. |
| **No QA/test-management-tool bridge.** | Test results stay in `verification-report.md`. The plugin does not push them to an external test-tracking system. |
| **The wiki mirror depends on an MCP tool being reachable.** | If the tool is not reachable, every stage still writes its local artifact and says so plainly. It does not block. The run mirrors nothing. |
| **The knowledge engine depends on Java 21+ and Maven being available locally** (checked once, at `/sdd.init`). | If it finds neither, the install skips grounding. The plugin gives the same one-line message and never blocks. |
| **No embedding model ships by default.** | Only BM25 lexical search works, until you add the optional vector-model files. See the Design principle section above. |

## Contributing

Read [`CLAUDE.md`](CLAUDE.md) first. There is no build. To prove a change, run
the affected `/sdd.*` command against a real feature-id in a real target repo.
Then read what it made.
