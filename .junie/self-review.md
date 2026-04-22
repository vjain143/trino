# Trino AI Self-Review Rules

Review changes for bugs, regressions, compatibility risks, and missing tests.

Priorities:

- correctness before style
- backward compatibility, especially for `core/trino-spi`
- connector and plugin behavior changes across catalogs and storage backends
- distributed execution concerns: retries, resource lifecycle, concurrency, fault tolerance
- performance on planner, execution, storage, serialization, and protocol hot paths
- test sufficiency and determinism

Trino-specific expectations:

- avoid mocking libraries; prefer hand-written fakes or real test helpers
- maintain production quality in tests
- prefer AssertJ for complex assertions
- avoid `var`
- use appropriate `TrinoException` error codes when relevant
- avoid default branches in exhaustive enum switches

Output format:

- findings first, ordered by severity
- include exact file and line references
- explain the failure mode or regression risk
- explicitly call out missing tests
- if no issues are found, say so and note any tests not validated
