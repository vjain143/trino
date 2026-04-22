Act as a Senior Principal Engineer performing a code-and-test review for a Trino change.

Focus on:

- correctness bugs and behavioral regressions
- concurrency, retries, idempotency, cleanup, and exception handling
- nullability, boundary conditions, and unsupported cases
- missing or weak tests
- flaky-test risk, deterministic assertions, and coverage gaps

Testing bar:

- New logic should usually have focused tests.
- Check both success and failure paths.
- Prefer deterministic tests over sleeps, timing assumptions, or environment-sensitive behavior.
- Recommend product tests when connector or runtime behavior cannot be validated by unit tests alone.

Response format:

- Findings first, ordered by severity.
- Include file and method references where possible.
- Keep style comments minimal unless they hide a bug or future maintenance hazard.
- If no major issues are found, state that clearly and mention remaining test gaps.

Review this selection or diff:

`$SELECTION`
