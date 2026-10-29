---
name: sdd.init
description: "One-time setup for agentic-sdd in a target repo: detects or confirms the profile and writes .agentic-sdd/config.yaml. Run once per repo before any other /sdd.* command."
---

# /sdd.init

One-time setup. Run it from the root of the target repo.

## What to do when invoked

1. If `.agentic-sdd/config.yaml` already exists, show its contents. Ask
   (AskUserQuestion) if the user wants to keep it, run detection again, or stop.
   You must not overwrite it without asking.

2. Detect the profile from the repo. Read the manifest or build file that is present
   (`pom.xml`, `package.json`, `build.gradle`, `requirements.txt`, etc.) and
   any existing lint or format config. Propose a profile name and a stack
   summary. This is a proposal only. Confirm it with the user
   with AskUserQuestion before you write anything.

3. Write `.agentic-sdd/config.yaml`:
   ```yaml
   profile: <confirmed-profile-name>
   stack: <language/framework summary>
   knowledgeEngine:
     enabled: true     # set false to skip knowledge-grounding entirely
     javaBin: ""        # resolved by step 4 below
     jarPath: ""         # resolved by step 4 below
     indexDir: ".agentic-sdd/knowledge-index"
     modelsDir: ".agentic-sdd/knowledge-engine-models"   # optional, see knowledge-engine/README.md
   wiki:
     enabled: true   # set false to skip the per-stage mirror entirely
     parentPageId: ""  # fill in if a wiki MCP tool is connected and the user has a parent page
   ```

4. **Build and index the bundled knowledge engine (one time, local, must not
   block setup):**
   a. Find a Java 21+ binary. Run `java -version`. If the major version is
      less than 21, or `java` is not found, look for one at a known install location
      (Windows: glob `C:/Program Files/Java/jdk-21*/bin/java.exe`; use the
      equivalent path on other OSes). If you find none, set
      `knowledgeEngine.enabled: false`. Tell the user plainly: "No Java 21+
      found. Knowledge-base grounding will be skipped. Everything else
      works normally." Then go to step 5.
   b. If `${CLAUDE_PLUGIN_ROOT}/knowledge-engine/target/knowledge-engine.jar`
      does not exist yet, build it one time: `mvn -q -DskipTests -f
      ${CLAUDE_PLUGIN_ROOT}/knowledge-engine/pom.xml package`. Use that Java
      21+ binary as `JAVA_HOME`. If Maven is not available or the build fails,
      do the same as in 4a: disable the engine, tell the user plainly, and continue.
   c. Run the `index` subcommand of the engine one time on the root of the
      target repo. Use **absolute** paths: `--index-dir
      <target-repo>/.agentic-sdd/knowledge-index --models-dir
      <target-repo>/.agentic-sdd/knowledge-engine-models`. No model is
      vendored by default, so the index uses BM25 only. This is expected and is not
      a failure (see `knowledge-engine/README.md` for the optional vector-
      search upgrade).
   d. Write the resolved absolute `javaBin` and `jarPath` back into
      `config.yaml`. Then later stages do not need to resolve them again.

5. Copy `specs/constitution-template.md` (from this plugin, at
   `${CLAUDE_PLUGIN_ROOT}/specs/constitution-template.md`) to
   `specs/constitution.md` in the target repo. Do this only if the file does not already exist there.
   Then help the user fill in the Profile section (stack, build command,
   lint/format command, test framework, required/forbidden patterns). Do not
   leave placeholders. **The conventions that you detect in the repo always
   win** over generic conventions. The file must describe what the repo
   really does, not an ideal version of it.

6. If `wiki.enabled` is true, try one wiki-MCP read (for example a space or page search call) to
   check that the wiki is reachable. If no such MCP tool is connected, or the call fails,
   set `wiki.enabled: false` in the config. Tell the user plainly: "No
   wiki tool connected — stage mirroring will be skipped. Local artifacts
   under `specs/{feature-id}/` remain the source of truth either way." You must not
   block setup on this.

7. Confirm that setup is complete. Show the user: the `.agentic-sdd/config.yaml` contents,
   the `specs/constitution.md` path, and the next command: `/sdd.require
   <feature-id>`.
