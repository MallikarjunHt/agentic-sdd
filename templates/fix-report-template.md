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
`implemented` (run `/sdd.verify` again) or `failed` (circuit breaker tripped —
see "If the circuit breaker trips" below)

## If the circuit breaker trips
After 3 failed attempts, stop. Do not try a 4th time. Tell the requester what
was tried, why it did not fix the problem, and if the plan needs a change
(go back to `/sdd.plan --force`) instead of another fix attempt.
