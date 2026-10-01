# CLAUDE.md — agentic-sdd (this plugin's own repo)

This file governs work *on* the agentic-sdd plugin itself, not on any target
repo it gets installed into (that's `specs/constitution.md` in the target
repo, written by `/sdd.init`).

## What "proven correct" means here

There is no build step and no test suite for this repo — it's prompts and
templates, not executable code. A change is proven by running the specific
`/sdd.*` command it touches against a real feature-id in a real target repo
(or a disposable scratch repo) and reading what it actually produced,
end to end if the change affects stage-to-stage handoff.

## Hard constraints

- **Never add a hardcoded path, import, or reference to another repo.**
  Everything this plugin needs at runtime comes through its own
  `${CLAUDE_PLUGIN_ROOT}`-relative paths or through the two optional MCP tool
  contracts documented in the README — nothing else. This is the one rule
  that must never be relaxed; it's the whole reason this repo exists as its
  own thing.
- **Every stage must degrade gracefully if an MCP tool isn't connected.**
  "The tool isn't there" is never a reason to block a stage — only to skip
  the one capability that tool provided, with a one-line note.
- **`status.json` is the only gate.** Don't add a second, conversational
  gating mechanism alongside it — that reintroduces the exact
  crash-doesn't-resume problem the status file exists to solve.
- **Don't duplicate a check across agents.** Each verify-stage agent (
  `sdd-verify-constitution`, `sdd-verify-quality-security`,
  `sdd-verify-tests`, `sdd-devil-advocate`) has an explicit "Scope
  discipline" section for exactly this reason — if two agents could flag the
  same thing, narrow one of their scopes instead of accepting the overlap.

## Standards

- Keep every agent/skill file self-contained — ponytail rules, standards,
  and scope boundaries live in the file itself, not in a shared include,
  because subagents don't inherit this CLAUDE.md. Deliberate duplication,
  not an oversight.
- New specialist roles need a trigger-keyword row added to the single table
  in `skills/implement/SKILL.md` — never a second, competing routing table
  somewhere else.
