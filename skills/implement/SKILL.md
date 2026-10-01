---
name: sdd.implement
description: "Stage 4 of the agentic-sdd pipeline. Executes 3-tasks.md's checklist, one task at a time, via a 12-role specialist roster. Status implemented or partial."
---

# /sdd.implement [feature-id]

**Precondition:** `specs/{feature-id}/status.json` stage must be `analyzed`
or `partial`. If the precondition fails, tell the user the current stage and stop.

## The 12-role trigger table

Single source of truth, referenced by `/sdd.plan` (owner finalization) and
here (actual routing). Score a task's text against these signals and pick the
winning role:

| Role | Agent | Triggers |
|---|---|---|
| `architect` | `sdd-architect` | design, architecture, how should we, system, trade-off, ADR, strategy |
| `seniorDev` | `sdd-senior-dev` | implement, refactor, review, PR, pull request, code review |
| `javaDev` | `sdd-java-dev` | .java, spring, springboot, maven, gradle, hibernate, JPA, @Bean, @Service |
| `angularDev` | `sdd-angular-dev` | .component.ts, .module.ts, angular, @Component, @NgModule, rxjs, observable |
| `uiDev` | `sdd-ui-dev` | CSS, SCSS, HTML, template, styling, layout, accessibility, Tailwind, Bootstrap, Material |
| `reactDev` | `sdd-react-dev` | react, jsx, tsx, useState, useEffect, Next.js, hook |
| `pythonDev` | `sdd-python-dev` | .py, django, fastapi, flask, pytest, pandas, pip |
| `devops` | `sdd-devops` | Dockerfile, k8s, kubernetes, pipeline, CI, CD, Helm, Terraform, kubectl, yaml |
| `qa` | `sdd-qa` | test, spec, assertion, bug, regression, scenario, cucumber, JUnit, Jest, coverage |
| `ba` | `sdd-ba` | requirement, user story, acceptance criteria, business rule, process, workflow, stakeholder |
| `dba` | `sdd-dba` | SQL, schema, migration, index, query, table, stored procedure, Flyway, Liquibase, ERD |
| `securityEng` | `sdd-security` | vulnerability, CVE, OWASP, auth, token, injection, XSS, pentest, security |

Ties go to: architect > seniorDev > any specialist. Default fallback: `seniorDev`.

## Model tier per task

Score each task's own text (not the whole feature) against these signals:

- **simple** (haiku): single file, rename, config change, < 3 files likely touched
- **medium** (sonnet, default): implement feature, bug fix, multi-file change, "add"/"fix"/"update"/"create"
- **complex** (opus): architecture decision, cross-cutting refactor, security-sensitive change, "design"/"how should we"/"trade-off"

## What to do when invoked

1. Re-read `status.json` — if stage is `partial`, resume at the first `[ ]`
   task rather than restarting from task 1.

2. For each unchecked `[ ]` task in `3-tasks.md`, in order:
   a. Use the owner already locked in at `/sdd.plan` step 4 — do **not**
      re-route here; routing happens once, at plan time.
   b. Score the task's own text for model tier (above).
   c. Spawn that task's owning agent **using the model tier scored in step
      b** (haiku/sonnet/opus) and this context: `specs/constitution.md`, the
      full `1-spec.md`, the full `2-plan.md`, the specific task text ("Task
      N: <text>"), the Definition of Done, and this appended instruction: "If
      this task contradicts the plan or the actual codebase, do not
      improvise — respond with `SPEC_DRIFT: <what the plan assumed> vs <what
      is actually true>` as the first line of your response instead of
      making changes."
   d. If the response's first line is `SPEC_DRIFT:` — stop the loop
      immediately. Set `status.json` stage to `partial`. Surface the drift
      to the user via AskUserQuestion: "Update the plan" / "Update the spec"
      / "Let me clarify" / "Abandon this task". Route to `/sdd.plan --force`
      or `/sdd.require --force` as appropriate; once resolved, re-run
      `/sdd.implement` to resume from this same task.
   e. Otherwise: mark the task `[x]` in `3-tasks.md` and continue.

3. Once every task is `[x]`: update `status.json` stage to `implemented`.

4. **Wiki mirror (optional, degrades gracefully):** create/update an
   "Implementation Summary" child page (task list, files changed, `git diff
   --stat`). Skip in one line if not connected.

5. Report status and the next command: `/sdd.verify {feature-id}`.
