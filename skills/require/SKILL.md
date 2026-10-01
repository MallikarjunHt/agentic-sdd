---
name: sdd.require
description: "Stage 1 (entry point) of the agentic-sdd pipeline. Turns a feature request or ticket reference into 1-spec.md, status reviewed."
---

# /sdd.require [feature-id]

**Precondition:** none — this is the pipeline's entry point. If `.agentic-sdd/config.yaml`
doesn't exist, tell the user to run `/sdd.init` first and stop.

## What to do when invoked

1. Resolve `feature-id`: use the arg if given, otherwise derive a short
   kebab-case slug from the request text (first ~6-8 significant words,
   lowercase, non-alphanumerics to `-`, truncate ~40 chars).

2. Create `specs/{feature-id}/` if it doesn't exist. If `1-spec.md` already
   exists there, read `status.json` and tell the user the current stage;
   ask (AskUserQuestion) whether to resume as-is, overwrite with
   `--force`, or pick a new feature-id.

3. **Knowledge grounding (optional, degrades gracefully):** if
   `knowledgeEngine.enabled` in config, run the bundled engine directly —
   `"<knowledgeEngine.javaBin>" -jar "<knowledgeEngine.jarPath>" search
   "<request text>" --index-dir "<abs path, knowledgeEngine.indexDir>"
   --models-dir "<abs path, knowledgeEngine.modelsDir>" --limit 8` — for
   relevant existing code/docs context. **Always use the absolute paths from
   config, never relative ones** — `--index-dir`/`--models-dir` resolve
   against the invoking shell's cwd, not the target repo. If the engine
   isn't built, Java isn't available, or the command errors, say so in one
   line and continue with no context — **never block a stage on this**.

4. Spawn `sdd-ba` with: any knowledge-grounding context from step 3, the
   target repo's `specs/constitution.md`, the original request text, and an
   instruction to write `specs/{feature-id}/1-spec.md` using
   `templates/spec-template.md`.

5. Show the full `1-spec.md` to the user. AskUserQuestion: "Approve —
   continue" / "Revise — describe changes" / "Cancel". Revise re-spawns
   `sdd-ba` with the delta appended and re-runs this gate. Cancel stops,
   leaving the spec on disk as a draft.

6. On approve: write/update `specs/{feature-id}/status.json`:
   ```json
   {"featureId": "{feature-id}", "profile": "<from config>", "stage": "reviewed", "fixAttempts": 0, "history": [{"stage": "reviewed", "at": "<timestamp>"}]}
   ```

7. **Wiki mirror (optional, degrades gracefully):** if `wiki.enabled`, create
   or update a ticket page (child of `wiki.parentPageId`) plus a child "Spec"
   page with `1-spec.md`'s content, via wiki MCP tools (expected contract:
   `mcp__confluence__confluence_createContent`/`confluence_updateContent`).
   Re-running this stage updates the existing pages, never duplicates them.
   If the MCP tool isn't connected, say so in one line and continue — the
   local file is the source of truth regardless.

8. Report the spec path and status, and the next command: `/sdd.plan
   {feature-id}`.
