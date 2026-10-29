---
name: sdd.tooling
description: "Shows which optional MCP tools (knowledge-base, wiki) are connected now, and what degrades if they are not. Read-only, no preconditions."
---

# /sdd.tooling

This command is read-only. It must not write anything.

## What to do when invoked

This plugin has exactly one optional *external* runtime dependency: the wiki
mirror, an MCP tool contract. The knowledge base is bundled (`knowledge-engine/`).
You build it locally one time with Java 21+ and Maven, and call it directly. No MCP
server and no other repo is involved.

| Capability | How it is provided | Used by | Degrades to |
|---|---|---|---|
| Knowledge base search | `search` subcommand of the bundled engine, with `java -jar` and the `javaBin`/`jarPath` recorded in `.agentic-sdd/config.yaml` | `/sdd.require`, `/sdd.plan` context-gathering | No context. The stage continues and says so in one line |
| Knowledge base indexing | `index` subcommand of the same engine, run one time by `/sdd.init` | one time per target repo (run `/sdd.init` again to refresh) | n/a |
| Knowledge base health | `doctor` subcommand of the same engine | the check in this command | Reported as "not built" (no jar yet) or "BM25 only" (no vector model) |
| Wiki page mirror | `mcp__confluence__confluence_createContent` / `confluence_updateContent` | the "Wiki mirror" step of every stage | Skipped. The local artifact is the only record. The stage says so in one line |

Check the knowledge engine. Run its `doctor` subcommand with the resolved
`javaBin`/`jarPath`/`indexDir`/`modelsDir` from the config. Report "not
built" if `knowledgeEngine.enabled` is false or the jar is missing. Do not
try to build it from here. That is the job of `/sdd.init`.

Check the wiki mirror. Try the lightest call available (for example a space search).

Report connected or not connected (or built or not built) for each capability. Do not
fail the whole command if one capability is missing. Report the status of the other one
in all cases. Finish with: "Local-only mode is always a valid way to run this
pipeline — status.json and the specs/ artifacts are the real source of truth
either way."
