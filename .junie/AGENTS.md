# Trino Agent Guidelines

## Repository shape

- This is a large multi-module Maven monorepo rooted at `/Users/vivek/Lab/GitHub/trino`.
- Main module groups:
  - `core/`: parser, SPI, engine, server, web UI.
  - `plugin/`: connectors and event listeners.
  - `lib/`: reusable shared libraries.
  - `service/`: proxy and verifier.
  - `testing/`: test harnesses, product tests, containers, benchmarks.
- The project uses Java 25, Maven Wrapper, JUnit 5, Airlift testing utilities, Error Prone, and module-level tests under `src/test`.

## Default review stance

- Optimize for correctness first, then safety, then performance, then style.
- Treat changes in `core/trino-spi`, `core/trino-main`, `core/trino-server*`, `plugin/trino-base-jdbc`, and shared `lib/` modules as high blast-radius.
- Be skeptical of changes that affect:
  - distributed query semantics
  - connector contracts and SPI compatibility
  - memory usage, buffering, and object allocation
  - concurrency, retries, idempotency, and failure handling
  - backward compatibility of configs, session properties, and wire-visible behavior

## Code review expectations

- Prefer findings with concrete evidence from code paths, tests, types, or module boundaries.
- Call out behavioral regressions before style issues.
- Check whether the change belongs in the current module or should be pushed down to SPI, toolkit, or connector-specific code.
- For connector changes, verify behavior across schema evolution, pushdown, type mapping, stats, predicate handling, and remote failure modes.
- For engine changes, verify planning, execution, error propagation, spill/exchange behavior, and memory accounting implications.
- For config changes, verify defaults, upgrade path, validation, and docs impact.

## Testing expectations

- Expect tests for new logic unless the change is a pure rename or dead-code removal.
- Prefer deterministic tests. Avoid wall-clock timing, sleeps, random data without fixed seeds, and locale/timezone assumptions.
- Check boundary cases: nulls, empty inputs, case sensitivity, large values, retries, partial failures, and unsupported remote capabilities.
- For parser/planner changes, look for focused unit tests in the owning module.
- For connector/runtime integration changes, consider whether a product test or container-backed test is warranted in `testing/`.
- Watch for tests that only assert success paths and miss failure paths or invariant checks.

## Build and verification hints

- Root build: `./mvnw clean install -DskipTests`
- Module tests: `./mvnw -pl <module> test -DskipITs`
- Trino requires JDK 25.

## Output style

- Be concise and technical.
- List findings ordered by severity.
- Reference concrete files and methods when possible.
- If no issues are found, say so and mention residual risks or testing gaps.
