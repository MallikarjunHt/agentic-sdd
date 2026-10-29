---
name: sdd.implement
description: "Stage 4 of the agentic-sdd pipeline. Runs the 3-tasks.md checklist, one task at a time, with a 12-role specialist roster. Status implemented or partial."
---

# /sdd.implement [feature-id]

**Precondition:** the stage in `specs/{feature-id}/status.json` must be `analyzed`
or `partial`. If the precondition fails, tell the user the current stage and stop.

## The 12-role trigger table

This table is the single source of truth. `/sdd.plan` refers to it (owner finalization) and
this file uses it (actual routing). Score the text of a task against these signals and pick the
role with the highest score:

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

Tie-break order: architect, then seniorDev, then any specialist. Default role: `seniorDev`.

## Model tier per task

Score the text of each task (not the whole feature) against these signals:

- **simple** (haiku): one file, rename, config change, fewer than 3 files likely to change
- **medium** (sonnet, default): implement a feature, bug fix, multi-file change, "add"/"fix"/"update"/"create"
- **complex** (opus): architecture decision, cross-cutting refactor, security-sensitive change, "design"/"how should we"/"trade-off"

## What to do when invoked

1. Read `status.json` again. If the stage is `partial`, resume at the first `[ ]`
   task. Do not start again from task 1.

2. For each unchecked `[ ]` task in `3-tasks.md`, in order:
   a. Use the owner that `/sdd.plan` step 4 already locked. You must **not**
      route again here. Routing happens one time, at plan time.
   b. Score the text of the task for model tier (see above).
   c. Spawn the owning agent of the task. Use the model tier from step b
      (haiku/sonnet/opus). Give the agent this context: `specs/constitution.md`, the
      full `1-spec.md`, the full `2-plan.md`, the text of the task ("Task
      N: <text>"), the Definition of Done, and this instruction at the end: "If
      this task contradicts the plan or the actual codebase, do not
      improvise. Respond with `SPEC_DRIFT: <what the plan assumed> vs <what
      is actually true>` as the first line of your response. Do not make
      changes."
   d. If the first line of the response is `SPEC_DRIFT:`, stop the loop
      at once. Set the `status.json` stage to `partial`. Show the drift
      to the user with AskUserQuestion: "Update the plan" / "Update the spec"
      / "Let me clarify" / "Abandon this task". Route to `/sdd.plan --force`
      or `/sdd.require --force` as needed. After the user resolves the drift, run
      `/sdd.implement` again to resume from this same task.
   e. If not, mark the task `[x]` in `3-tasks.md` and continue.

3. When every task is `[x]`, set the `status.json` stage to `implemented`.

4. **Wiki mirror (optional, degrades gracefully):** make or update an
   "Implementation Summary" child page (task list, files changed, `git diff
   --stat`). If the wiki is not connected, skip this step and say so in one line.

5. Report the status and the next command: `/sdd.verify {feature-id}`.
