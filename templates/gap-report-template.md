# Gap Report: <Title>

**Feature ID:** <feature-id>
**Author:** sdd-gap-analyst
**Plan:** ./2-plan.md

## Summary
<one paragraph: does the plan, as written, fully satisfy the spec's acceptance
criteria and Definition of Done against the actual current codebase?>

## Findings
| # | Severity | Area | Finding | Recommendation |
|---|---|---|---|---|
| 1 | CRITICAL / MAJOR / MINOR | <file or concern> | <what's missing or wrong> | <resolve / descope / clarify> |

## Resolution
For each CRITICAL finding, record how it was resolved before moving on:
- Resolved in plan (re-run `/sdd.plan --force`)
- Descoped (added to spec's Out of scope, re-run `/sdd.require --force` if the
  spec text itself needs the explicit descope recorded)
- Accepted as a known limitation (requires requester sign-off, record who/when)

## Verdict
`gaps-found` (CRITICAL findings open) or `analyzed` (none open, or all resolved)
