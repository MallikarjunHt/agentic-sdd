# Verification Report: <Title>

**Feature ID:** <feature-id>
**Plan:** ./2-plan.md

## Constitution check (sdd-verify-constitution)
<pass/fail against specs/constitution.md's profile rules, with specifics>

## Quality & security check (sdd-verify-quality-security)
<pass/fail — code quality, security-sensitive patterns, dependency concerns>

## Test check (sdd-verify-tests)
<pass/fail — do the tests the plan called for exist, pass, and actually
exercise the acceptance criteria rather than just the happy path>

## Devil's advocate pass (sdd-devil-advocate)
<adversarial review: what would make this fail in production that the above
three checks wouldn't catch — concurrency, data migration, rollout order,
backward compatibility>

## Overall verdict
`PASS` / `CONCERNS` (list) / `FAIL` (list failing Definition of Done items)

## CLAUDE.md sync proposal (only on PASS)
<diff proposed to the target repo's CLAUDE.md, to be committed on the feature
branch and reviewed in the normal PR — never a silent separate commit>
