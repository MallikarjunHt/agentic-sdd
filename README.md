# Agentic-SDD

Agentic, spec-driven development: one feature request in — a spec, a plan, a
gap analysis, specialist-implemented code, and a verification report out —
with every stage gated by a persistent on-disk status file, not conversation
state. **Fully standalone**: this repo has no dependency, import, submodule,
or hardcoded path pointing at any other repo or plugin. It installs and runs
on its own.

## Design principle: the knowledge base is bundled; only the wiki mirror is MCP-only

The knowledge-base engine (`knowledge-engine/` — a renamed, standalone fork of
a Lucene-based BM25/vector search tool, MIT licensed, see its own README) is
vendored **source**, not a binary, inside this repo. `/sdd.init` builds it
once locally with Java 21+ and Maven, then every later stage invokes the jar
directly via `java -jar` — no MCP server, no other repo, in the loop. It keeps
working even if every other plugin/MCP server on the machine is uninstalled.
No embedding model ships with it (that would be ~90MB of binary weight for a
plugin that's otherwise prompts and templates) — it indexes and searches
BM25-only out of the box. Vector search is an optional upgrade: drop
`model.onnx`+`vocab.txt` into `.agentic-sdd/knowledge-engine-models/` (see
`knowledge-engine/README.md` for a known-good source) and nothing else needs
reconfiguring.

The **wiki mirror** is the one remaining optional *external* dependency — a
real external system (Confluence or similar), checked at runtime via
`mcp__confluence__confluence_createContent`/`confluence_updateContent`. Every
stage that calls it degrades gracefully and says so in one line if it isn't
connected. Local artifacts under `specs/{feature-id}/` are always the real
source of truth either way. Run `/sdd.tooling` any time to see what's
currently built/connected. Concretely, this shows up as a "Knowledge
grounding (optional, degrades gracefully)" step in `require`/`plan` and a
"Wiki mirror (optional, degrades gracefully)" step in every stage that writes
an artifact.

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
> Plugins load at session start. Restart or reload Claude Code after
> installing, or the `sdd.*` commands and `sdd-*` agents won't appear.

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

Every stage after `require` is gated on `specs/{feature-id}/status.json`, so
the pipeline cannot run out of order and a crashed run resumes instead of
restarting. See [`run_guideline.md`](run_guideline.md) for session discipline
on a real run.

## What's here beyond a plain spec->plan->code->review loop

- **A dedicated gap-analysis stage** (`sdd-gap-analyst`, `/sdd.analyze`) that
  checks the plan against the spec's acceptance criteria *and* the real
  current codebase before any code gets written — CRITICAL findings block
  progress until resolved or explicitly descoped/accepted.
- **A three-way verify split plus an adversarial pass**: constitution
  conformance, quality/security, and test adequacy are checked by three
  separate agents so no single check has to cover everything, then
  `sdd-devil-advocate` looks for what a checklist-shaped review structurally
  can't catch (concurrency, migration/rollback risk, backward compatibility).
- **A fix stage with a real circuit breaker** — `sdd-fixer` gets 3 attempts
  per verify FAIL before it has to stop and recommend a plan change instead
  of looping indefinitely.
- **A `CLAUDE.md` sync, proposed once, only on verify PASS** — committed to
  the same feature branch, reviewed in the normal PR, never a silent commit.
- **A profile/constitution system** — one plugin, any number of target
  repos, each grounded in that repo's own real conventions
  (`specs/constitution.md`), not a generic textbook standard. The repo's own
  detected config always wins over this file when they disagree.
- **A 12-role specialist roster with keyword-based routing** (see
  `skills/implement/SKILL.md`) plus complexity-tiered model selection
  (haiku/sonnet/opus) per task — broader stack coverage than a single
  general-purpose "builder" agent, and cost-aware.

## Known gaps

Stated plainly, so nobody plans around a component that isn't there yet:

| Gap | Impact |
|---|---|
| **No automated validation of this plugin itself.** | Nothing here is checked by a compiler or test suite — a change is proven by running the affected `/sdd.*` command against a real feature. See [`CLAUDE.md`](CLAUDE.md). |
| **No cross-repo coordination.** | Each install targets one repo via its own `.agentic-sdd/config.yaml`; a feature spanning two repos needs two separate runs, manually coordinated. |
| **No QA/test-management-tool bridge.** | Test results live in `verification-report.md` only, not pushed to any external test-tracking system. |
| **The wiki mirror depends on an MCP tool being reachable.** | If it isn't, every stage still writes its local artifact and says so plainly rather than blocking — nothing is mirrored in that run. |
| **The knowledge engine depends on Java 21+ and Maven being available locally** (checked once, at `/sdd.init`). | If neither is found, grounding is skipped for the whole install — same one-line, never-blocks degrade. |
| **No embedding model ships by default.** | BM25 lexical search only, until the optional vector-model files are added — see the Design principle section above. |

## Contributing

Read [`CLAUDE.md`](CLAUDE.md) first. There is no build; a change is proven by
running the affected `/sdd.*` command against a real feature-id in a real
target repo and reading what it produced.
