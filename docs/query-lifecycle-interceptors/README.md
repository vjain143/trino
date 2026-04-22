# Query Request Lifecycle and Interceptors

This README maps where a user query enters Trino, where you can control it before execution, how lifecycle status is managed, and where custom interception is possible.

For full architecture, file-by-file changes, OPA examples, and Kafka/MySQL integration details, see [query-approval-workflow.md](./query-approval-workflow.md).

## Entry point for user query

1. HTTP entry: `POST /v1/statement`
   - `core/trino-main/src/main/java/io/trino/dispatcher/QueuedStatementResource.java`
   - Method: `postStatement(...)`
2. Query object registration:
   - `QueuedStatementResource.registerQuery(...)`
3. First submit to dispatcher:
   - `QueuedStatementResource.Query.submitIfNeeded(...)`
   - Calls `DispatchManager.createQuery(...)`

## Control before execution

Main pre-execution control is in:

- `core/trino-main/src/main/java/io/trino/dispatcher/DispatchManager.java`
- Method: `createQueryInternal(...)`
- Permission check: `accessControl.checkCanExecuteQuery(...)`

Use a custom `SystemAccessControl` plugin to enforce policy before execution:

- SPI: `core/trino-spi/src/main/java/io/trino/spi/security/SystemAccessControl.java`
- Hook: `checkCanExecuteQuery(Identity identity, QueryId queryId)`

## Approval workflow status

This repository now supports `APPROVAL_IN_FLIGHT` for OPA authorization denials
where OPA returns:

```json
{
  "result": false,
  "reason": "Approval Required"
}
```

Behavior:

- OPA plugin throws an authorization deny with the `Approval Required` message.
- Failed dispatch maps such denials to query state `APPROVAL_IN_FLIGHT`.
- `/v1/statement` and query metadata report `APPROVAL_IN_FLIGHT` for that query.
- Query completion events include `queryState=APPROVAL_IN_FLIGHT`.

## Where PLANNING/RUNNING/COMPLETED status comes from

Query states are defined in:

- `core/trino-main/src/main/java/io/trino/execution/QueryState.java`

Important states include:

- `PLANNING`
- `RUNNING`
- `APPROVAL_IN_FLIGHT` (approval pending)
- `FINISHED` (this is the terminal "completed" state)
- `FAILED`

Core state transitions happen in:

- `core/trino-main/src/main/java/io/trino/execution/QueryStateMachine.java`
  - `transitionToPlanning()`
  - `transitionToRunning()`
  - `transitionToFinishing()`
  - `transitionToFinishedIfReady()`

Planning starts from:

- `core/trino-main/src/main/java/io/trino/execution/SqlQueryExecution.java`
  - `start()`

## How to customize and add interceptors

### Option A: no core patch (recommended first)

1. Pre-execution allow/deny interceptor:
   - Implement `SystemAccessControl.checkCanExecuteQuery(...)`.
2. Lifecycle event interceptor (audit/monitoring):
   - Implement `EventListener`:
     - `queryCreated(QueryCreatedEvent)`
     - `queryCompleted(QueryCompletedEvent)`
   - SPI: `core/trino-spi/src/main/java/io/trino/spi/eventlistener/EventListener.java`
   - Runtime manager: `core/trino-main/src/main/java/io/trino/eventlistener/EventListenerManager.java`

Note: event listeners are observational. They do not block query execution.

### Option B: custom state-transition interceptor (requires core changes)

If you need callbacks on every internal state change (`QUEUED -> PLANNING -> RUNNING -> FINISHED/FAILED`), add logic around:

- `QueryStateMachine.addStateChangeListener(...)`
- `QueryManager.addStateChangeListener(...)`

This path is not currently exposed as a dedicated SPI for external plugins.

## Quick lifecycle summary

`POST /v1/statement` -> `QueuedStatementResource` -> `DispatchManager` -> `LocalDispatchQuery` -> `QueryManager.createQuery(...)` -> `SqlQueryExecution.start()` -> state transitions in `QueryStateMachine`.

## Sending query events to external systems

Use Trino event listeners. Query events already include query text and query
state, and can be shipped to Kafka or MySQL without additional core code.

### Kafka topic

`etc/kafka-event-listener.properties`:

```properties
event-listener.name=kafka
kafka-event-listener.broker-endpoints=kafka.example.com:9093
kafka-event-listener.created-event.topic=query_create
kafka-event-listener.completed-event.topic=query_complete
kafka-event-listener.client-id=trino-approval-flow
```

`etc/config.properties`:

```properties
event-listener.config-files=etc/kafka-event-listener.properties
```

### MySQL database

`etc/mysql-event-listener.properties`:

```properties
event-listener.name=mysql
mysql-event-listener.db.url=jdbc:mysql://example.net:3306/trino_events?user=trino&password=secret
```

`etc/config.properties`:

```properties
event-listener.config-files=etc/mysql-event-listener.properties
```
