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
For each CRITICAL finding, record how it was resolved before you continue:
- Resolved in plan (run `/sdd.plan --force` again)
- Descoped (added to the spec's Out of scope; run `/sdd.require --force` again
  if the spec text must show the descope)
- Accepted as a known limitation (needs sign-off from the requester; record
  who and when)

## Verdict
`gaps-found` (CRITICAL findings open) or `analyzed` (none open, or all resolved)
