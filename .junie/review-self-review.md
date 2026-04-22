# Trino AI Self-Review Rules

Review code changes with the following priorities:

1. Correctness and regressions
   - Look for behavior changes, invalid assumptions, broken invariants, and missing edge-case handling.
   - Prioritize distributed-query and connector correctness over style.

2. Architecture and blast radius
   - Flag changes that cross module boundaries in a way that increases coupling.
   - Highlight risky edits in `core/trino-spi`, `core/trino-main`, `core/trino-server*`, `plugin/trino-base-jdbc`, and shared `lib/` modules.
   - Check whether the abstraction belongs in SPI, shared toolkit, or a connector-specific module.

3. Performance and resource usage
   - Look for avoidable allocations, buffering, repeated remote calls, unbounded collections, blocking behavior, and memory-accounting gaps.
   - For planner and execution code, consider large-cluster and large-table behavior, not just happy-path correctness.

4. Reliability and failure handling
   - Check retries, timeouts, partial-failure handling, cleanup, exception mapping, and log signal quality.
   - Call out non-deterministic or flaky behavior risks.

5. Tests
   - Verify that tests cover both success and failure paths.
   - Prefer deterministic tests with fixed inputs and no time or locale dependence.
   - Recommend product or container-backed tests when unit tests are insufficient for connector/runtime behavior.

Do not spend much space on formatting unless it hides a bug or maintenance risk.
Prefer high-signal findings with file references and a short explanation of impact.
