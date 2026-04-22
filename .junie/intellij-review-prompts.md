# IntelliJ Review Prompts

Add these as custom prompts in IntelliJ AI Assistant Prompt Library. Use Junie in Ask mode for read-only repo exploration, and switch to Code mode only if you want it to propose or apply fixes.

## 1. Senior Principal Architect Review

Review this change like a Senior Principal Architect for Trino.

Focus on API and SPI stability, module boundaries, dependency direction, distributed-systems correctness, operability, configuration compatibility, upgrade risk, and long-term maintainability.

Prioritize findings over summary. List only concrete issues, ordered by severity. For each finding, include:

- why it is a problem
- what behavior or contract can break
- the exact file and line reference
- what test or validation is missing

Ignore minor style nits unless they hide a maintenance or correctness problem.

## 2. Senior Principal Engineer Review

Review this change like a Senior Principal Engineer for Trino.

Focus on implementation correctness, edge cases, state handling, concurrency, resource lifecycle, exception behavior, performance on hot paths, and consistency with surrounding code.

Prioritize findings over summary. List only concrete bugs, regressions, hidden behavior changes, or missing tests, ordered by severity. For each item, include the exact file and line reference and a short explanation of the failure mode.

## 3. Test Review

Review this change only from a test strategy and regression-prevention perspective.

Check whether the tests match the changed behavior, whether negative paths and edge cases are covered, whether the tests are deterministic, and whether the right test layer was used:

- unit tests in the module
- integration tests in `testing/`
- product tests in `testing/trino-product-tests`

Call out missing assertions, flaky patterns, untested failure modes, and places where Trino guidance says a hand-written fake should be used instead of a mocking library.

## Suggested Workflow

1. Put Junie in Ask mode and ask it to inspect only the modules touched by the diff.
2. Use one of the prompts above in AI Assistant on the selected diff or files.
3. If the review finds likely issues, switch Junie to Code mode and ask it to prepare a minimal fix plus targeted tests.
