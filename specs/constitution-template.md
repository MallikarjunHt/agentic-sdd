# Constitution

`/sdd.init` writes this file once. Every stage reads it. It is based on the
real conventions of the *target* repo. It must not hold generic best
practices or rules copied from another repo. If a rule below conflicts with
what the stage finds in the repo (a linter config, an existing test pattern,
a build file), **the repo wins**. Update this file to match the repo. Do not
change the repo to match this file.

## Profile: <profile-name>

**Target repo:** <repo name/path>
**Stack:** <language, framework, versions>
**Build:** <build tool and command, e.g. `mvn clean install`, `npm run build`>
**Lint/format command (run before every commit):** <command>
**Test framework:** <framework — the ONLY one `sdd-verify-tests` should expect;
  flag any task that uses a different one as spec drift, not a free choice>

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
<anything else a specialist needs, so they do not have to find it again>
