---
name: sdd.tooling
description: "Shows which optional MCP tools (knowledge-base, wiki) are currently connected, and what degrades if they aren't. Read-only, no preconditions."
---

# /sdd.tooling

Read-only — never writes anything.

## What to do when invoked

This plugin has exactly two optional runtime dependencies, both MCP tool
contracts, never vendored code or a hardcoded path to another repo:

| Capability | Expected tool | Used by | Degrades to |
|---|---|---|---|
| Knowledge base search | `mcp__lucenedb__lucenedb_search` (any MCP server exposing this tool name) | `/sdd.require`, `/sdd.plan` context-gathering | No context — stage proceeds, says so in one line |
| Knowledge base indexing | `mcp__lucenedb__lucenedb_index` | not called automatically by any stage; available for manual re-indexing | n/a |
| Knowledge base health | `mcp__lucenedb__lucenedb_doctor` | this command's own check | Reported as "not connected" |
| Wiki page mirror | `mcp__confluence__confluence_createContent` / `confluence_updateContent` | every stage's "Wiki mirror" step | Skipped, local artifact remains the only record, says so in one line |

Check each by attempting the lightest-weight call available (e.g.
`lucenedb_doctor`, a wiki space search) and report connected/not-connected
per capability — don't fail the whole command if one tool is missing, report
the other's status regardless. Finish with: "Local-only mode is always a
valid way to run this pipeline — status.json and the specs/ artifacts are
the real source of truth either way."
