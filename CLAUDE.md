# CLAUDE.md — agentic-sdd (this plugin's own repo)

This file governs work *on* the agentic-sdd plugin itself. It does not govern
any target repo that gets the plugin. For a target repo, the rules are in
`specs/constitution.md`, which `/sdd.init` writes.

## What "proven correct" means here

This repo has no build step and no test suite. It has prompts and templates,
not executable code. To prove a change, run the `/sdd.*` command that the
change touches. Use a real feature-id in a real target repo (or a disposable
scratch repo). Read what the command made. If the change affects the handoff
from stage to stage, do this end to end.

## Hard constraints

- **Never add a hardcoded path, import, or reference to another repo at
  runtime.** `knowledge-engine/` is a renamed, vendored *copy* of the source
  of another project. Its own README explains the provenance and license.
  Nothing in it, or anywhere else in this plugin, points back to the repo
  path of that project. Everything needed at runtime comes from
  `${CLAUDE_PLUGIN_ROOT}`-relative paths, the config-resolved
  `knowledgeEngine.javaBin`/`jarPath`, or the one remaining optional MCP tool
  contract (the wiki mirror). Nothing else is allowed. This rule must never
  be relaxed. It is the reason this repo exists as its own project.
- **Every stage must degrade gracefully if an MCP tool is not connected.**
  A missing tool is never a reason to block a stage. Skip only the one
  capability that the tool gave, and add a one-line note.
- **`status.json` is the only gate.** Do not add a second, conversational
  gating mechanism with it. That brings back the problem that the status file
  solves: a crash does not resume.
- **Do not duplicate a check across agents.** Each verify-stage agent
  (`sdd-verify-constitution`, `sdd-verify-quality-security`,
  `sdd-verify-tests`, `sdd-devil-advocate`) has a "Scope discipline" section
  for this reason. If two agents can flag the same thing, make the scope of
  one agent narrower. Do not accept the overlap.

## Standards

- Keep every agent/skill file self-contained. Put ponytail rules, standards,
  and scope boundaries in the file itself, not in a shared include. Subagents
  do not inherit this CLAUDE.md. The duplication is deliberate.
- A new specialist role needs a trigger-keyword row in the single table in
  `skills/implement/SKILL.md`. Never add a second, competing routing table
  in another place.
