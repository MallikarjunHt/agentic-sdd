---
name: sdd.init
description: "One-time setup for agentic-sdd in a target repo: detects or confirms the profile and writes .agentic-sdd/config.yaml. Run once per repo before any other /sdd.* command."
---

# /sdd.init

One-time setup. Run from the target repo's root.

## What to do when invoked

1. If `.agentic-sdd/config.yaml` already exists, show its contents and ask
   (AskUserQuestion) whether to keep it, re-run detection, or abort. Don't
   overwrite silently.

2. Detect the profile from the repo: read the manifest/build file present
   (`pom.xml`, `package.json`, `build.gradle`, `requirements.txt`, etc.) and
   any existing lint/format config, to propose a profile name and stack
   summary. This is a proposal, not a guess locked in — confirm with the user
   via AskUserQuestion before writing anything.

3. Write `.agentic-sdd/config.yaml`:
   ```yaml
   profile: <confirmed-profile-name>
   stack: <language/framework summary>
   knowledgeBase:
     enabled: true   # set false to skip knowledge-grounding entirely
   wiki:
     enabled: true   # set false to skip the per-stage mirror entirely
     parentPageId: ""  # fill in if a wiki MCP tool is connected and the user has a parent page
   ```

4. Copy `specs/constitution-template.md` (from this plugin, read via
   `${CLAUDE_PLUGIN_ROOT}/specs/constitution-template.md`) to the target
   repo's `specs/constitution.md` if it doesn't already exist there, and walk
   the user through filling in the Profile section (stack, build command,
   lint/format command, test framework, required/forbidden patterns) rather
   than leaving placeholders. **The repo's own detected conventions always
   win** over anything generic — this file should describe what the repo
   actually does, not an idealized version of it.

5. If `wiki.enabled`, try one wiki-MCP read (e.g. a space/page search call) to
   confirm reachability. If no such MCP tool is connected or the call fails,
   set `wiki.enabled: false` in the config and tell the user plainly: "No
   wiki tool connected — stage mirroring will be skipped. Local artifacts
   under `specs/{feature-id}/` remain the source of truth either way." Never
   block setup on this.

6. Confirm completion and show the user: `.agentic-sdd/config.yaml` contents,
   `specs/constitution.md` path, and the next command: `/sdd.require
   <feature-id>`.
