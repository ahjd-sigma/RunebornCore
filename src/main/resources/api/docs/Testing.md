# Testing Service

## 1) Overview & Features

- Lightweight service verification triggered manually via command
- Central test suite with per-service test classes

## 2) How to Use

### Command
- `/runeborn test` — runs the full suite and reports results in chat/console

## 3) Covered Tests

- Config Service: create/read/write/defaults
- Commands Service: register main/sub commands
- Debugger Service: logging and exception capture
- GUI Service: service availability
- Database Service: basic CRUD repository flow
- Ticker Service: lifecycle controls (start/pause/resume/stop)
- Service Resolution: `ServiceHub.isReady()` vs `Services.get/require`

## 4) Notes

- Tests do not block server start; they run only on demand.
- Extend tests as features grow; each service has a dedicated test class under `src/main/kotlin/com/runeborn/core/tests/`.
 - Prefer using `ServiceHub` in new tests; use `Services` only for fallback scenarios.
