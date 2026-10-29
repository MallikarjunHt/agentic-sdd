---
name: sdd.require
description: "Stage 1 (entry point) of the agentic-sdd pipeline. Turns a feature request or ticket reference into 1-spec.md, status reviewed."
---

# /sdd.require [feature-id]

**Precondition:** none. This is the entry point of the pipeline. If `.agentic-sdd/config.yaml`
does not exist, tell the user to run `/sdd.init` first and stop.

## What to do when invoked

1. Resolve `feature-id`. If the user gave an argument, use it. If not, make a short
   kebab-case slug from the request text: the first 6-8 significant words,
   lowercase, non-alphanumerics changed to `-`, cut to about 40 characters.

2. Make `specs/{feature-id}/` if it does not exist. If `1-spec.md` already
   exists there, read `status.json` and tell the user the current stage.
   Ask (AskUserQuestion) if the user wants to resume as-is, overwrite with
   `--force`, or pick a new feature-id.

3. **Knowledge grounding (optional, degrades gracefully):** if
   `knowledgeEngine.enabled` is true in the config, run the bundled engine directly:
   `"<knowledgeEngine.javaBin>" -jar "<knowledgeEngine.jarPath>" search
   "<request text>" --index-dir "<abs path, knowledgeEngine.indexDir>"
   --models-dir "<abs path, knowledgeEngine.modelsDir>" --limit 8`.
   This gives relevant context from existing code and docs. **You must always use the absolute paths from
   the config. You must not use relative paths.** `--index-dir` and `--models-dir` resolve
   against the cwd of the calling shell, not the target repo. If the engine
   is not built, Java is not available, or the command gives an error, say so in one
   line and continue with no context. **You must not block a stage on this.**

4. Spawn `sdd-ba` with: any knowledge-grounding context from step 3, the
   `specs/constitution.md` of the target repo, the original request text, and an
   instruction to write `specs/{feature-id}/1-spec.md` with
   `templates/spec-template.md`.

5. Show the full `1-spec.md` to the user. Use AskUserQuestion: "Approve —
   continue" / "Revise — describe changes" / "Cancel". For Revise, spawn
   `sdd-ba` again with the changes appended and run this gate again. For Cancel, stop
   and leave the spec on disk as a draft.

6. If the user approves: write or update `specs/{feature-id}/status.json`:
   ```json
   {"featureId": "{feature-id}", "profile": "<from config>", "stage": "reviewed", "fixAttempts": 0, "history": [{"stage": "reviewed", "at": "<timestamp>"}]}
   ```

7. **Wiki mirror (optional, degrades gracefully):** if `wiki.enabled` is true, make
   or update a ticket page (child of `wiki.parentPageId`) and a child "Spec"
   page with the content of `1-spec.md`. Use the wiki MCP tools (expected contract:
   `mcp__confluence__confluence_createContent`/`confluence_updateContent`).
   When you run this stage again, update the existing pages. Do not make duplicates.
   If the MCP tool is not connected, say so in one line and continue. The
   local file is always the source of truth.

8. Report the spec path and the status, and the next command: `/sdd.plan
   {feature-id}`.
