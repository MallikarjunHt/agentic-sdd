---
name: sdd.tooling
description: "Shows which optional MCP tools (knowledge-base, wiki) are currently connected, and what degrades if they aren't. Read-only, no preconditions."
---

# /sdd.tooling

Read-only — never writes anything.

## What to do when invoked

This plugin has exactly one optional *external* runtime dependency — the wiki
mirror, an MCP tool contract. The knowledge base is bundled (`knowledge-engine/`),
built locally once with Java 21+ and Maven, and invoked directly — no MCP
server, no other repo, involved:

| Capability | How it's provided | Used by | Degrades to |
|---|---|---|---|
| Knowledge base search | Bundled engine's `search` subcommand, via `java -jar` using the `javaBin`/`jarPath` recorded in `.agentic-sdd/config.yaml` | `/sdd.require`, `/sdd.plan` context-gathering | No context — stage proceeds, says so in one line |
| Knowledge base indexing | Same engine's `index` subcommand, run once by `/sdd.init` | one-time per target repo (re-run `/sdd.init` to refresh) | n/a |
| Knowledge base health | Same engine's `doctor` subcommand | this command's own check | Reported as "not built" (no jar yet) or "BM25 only" (no vector model) |
| Wiki page mirror | `mcp__confluence__confluence_createContent` / `confluence_updateContent` | every stage's "Wiki mirror" step | Skipped, local artifact remains the only record, says so in one line |

Check the knowledge engine by running its `doctor` subcommand with the
config's resolved `javaBin`/`jarPath`/`indexDir`/`modelsDir` (report "not
built" if `knowledgeEngine.enabled` is false or the jar is missing — don't
try to build it from here, that's `/sdd.init`'s job). Check the wiki mirror
by attempting the lightest-weight call available (e.g. a space search).
Report connected/not-connected (or built/not-built) per capability — don't
fail the whole command if one is missing, report the other's status
regardless. Finish with: "Local-only mode is always a valid way to run this
pipeline — status.json and the specs/ artifacts are the real source of truth
either way."
