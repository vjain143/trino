# IntelliJ AI Review Setup For Trino

This repository contains reusable review guidance for JetBrains Junie and AI Assistant.

## Files added for IntelliJ

- `.junie/AGENTS.md`: persistent repo guidance for Junie agent tasks
- `.junie/review-self-review.md`: rules file for AI Assistant Self-Review
- `.junie/prompts/senior-principal-architect.md`: prompt template for architecture review
- `.junie/prompts/senior-principal-engineer-test-review.md`: prompt template for code and test review

## Recommended setup

### 1. Install plugins

- Install `Junie` if you want an autonomous coding agent inside IntelliJ.
- Install `AI Assistant` if you want AI Chat, Prompt Library, and Self-Review with AI.

### 2. Configure Junie for this repo

Open `Settings | Tools | Junie | Project Settings` and set:

- `Project path`: `/Users/vivek/Lab/GitHub/trino`
- `Guidelines path`: `.junie/AGENTS.md`
- `Enabled technologies`: Java, Maven, SQL, Shell, Markdown, JavaScript if you work in `core/trino-web-ui`

Use:

- `Ask` mode for repo exploration and review-only work
- `Code` mode for implementation, refactors, and test generation

### 3. Configure AI Assistant Self-Review

Open `Settings | Tools | AI Assistant | Project Settings` and set:

- `Path to rules for AI Self-Review`: `.junie/review-self-review.md`

Then run self-review from the Commit tool window on selected changes.

### 4. Create saved reviewer prompts

Open `Settings | Tools | AI Assistant | Prompt Library` and create prompts from:

- `.junie/prompts/senior-principal-architect.md`
- `.junie/prompts/senior-principal-engineer-test-review.md`

Recommended prompt names:

- `Architect Review`
- `Engineer + Test Review`

Enable:

- `Show prompt in AI Actions popup`
- `Wait for additional user input after invoking`

## How to use the reviewers

### Senior Principal Architect

Use for:

- changes touching SPI, engine planning/execution, cross-module abstractions, shared libraries, or connector architecture

Suggested invocation:

- select the diff or files
- run `Architect Review`
- add a short note like `Review this as if it were a high-risk production change`

### Senior Principal Engineer and Test Review

Use for:

- bug fixes, connector changes, reliability changes, and anything where test adequacy matters

Suggested invocation:

- select the diff or files
- run `Engineer + Test Review`
- add a short note like `Focus on regressions, missing edge cases, and missing tests`

## Optional third prompt

If you want a dedicated test-only reviewer, duplicate `Engineer + Test Review` in the Prompt Library and reduce the scope to:

- missing cases
- flaky tests
- deterministic assertions
- product-test coverage gaps

## Practical workflow

1. Use Junie `Ask` mode to explore impacted modules.
2. Use `Architect Review` on broad design changes.
3. Use `Engineer + Test Review` on the final diff.
4. Run `Self-Review with AI` before commit for a last pass tied to the selected changes.

## Trino-specific review heuristics

- Be extra careful in `core/trino-spi`, `core/trino-main`, `core/trino-server*`, and shared `lib/` modules.
- For connector changes, review pushdown, type mapping, retries, remote failure modes, and product-test coverage.
- For tests, prefer focused module tests first and add product tests when integration behavior is the real risk.
