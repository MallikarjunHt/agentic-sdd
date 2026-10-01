# Constitution

Written once by `/sdd.init`, read by every stage. This file grounds the
pipeline in the *target* repo's real conventions — never generic best
practices, and never another repo's rules copied over. If anything below
conflicts with what the stage actually finds in the repo (a linter config, an
existing test pattern, a build file), **the repo wins** — update this file to
match, don't force the repo to match this file.

## Profile: <profile-name>

**Target repo:** <repo name/path>
**Stack:** <language, framework, versions>
**Build:** <build tool and command, e.g. `mvn clean install`, `npm run build`>
**Lint/format command (run before every commit):** <command>
**Test framework:** <framework — the ONLY one `sdd-verify-tests` should expect;
  flag any task that reaches for a different one as spec drift, not a free choice>

## Required patterns
<things every change in this profile must do — e.g. a validation call that
must run before a specific utility, a required dual-check between two code
paths, a required annotation on new endpoints>

## Forbidden patterns
<things never allowed in this profile — e.g. a mocking approach that doesn't
match how this repo actually tests, a deprecated API, a banned dependency>

## Known architectural splits
<e.g. two client types that must be branched on, two delivery mechanisms that
must both be covered by any change touching a shared path>

## Notes
<anything else a specialist needs to not have to re-discover from scratch>
