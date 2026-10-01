# Fix Log: <Title>

**Feature ID:** <feature-id>
**Author:** sdd-fixer
**Verification report:** ./verification-report.md
**Attempt:** <n> of 3 (circuit breaker — see skills/fix/SKILL.md)

## Failing items addressed
<the specific FAIL / CONCERNS items from the verification report this attempt targets>

## Changes made
<bullets, file-level>

## Result
`implemented` (re-run `/sdd.verify`) or `failed` (circuit breaker tripped —
see "If the circuit breaker trips" below)

## If the circuit breaker trips
After 3 failed attempts, stop. Do not retry a 4th time. Surface to the
requester: what was tried, why it didn't resolve, and whether this needs a
plan change (back to `/sdd.plan --force`) rather than another fix attempt.
