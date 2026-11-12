---
status: accepted
date: 2026-11-11
ticket: https://marstech.myjetbrains.com/youtrack/issue/MLS-220
---

# Cron jobs run on Spring's in-process TaskScheduler, persisted in MongoDB

## Context and Problem Statement

MLS-220 adds user-defined cron jobs (HTTP webhook first) to sol4.space. The app runs as a single
App Platform instance with 512 MB, and its heap already plateaus at 72-80 % (MLS-238). Which engine
schedules and persists the jobs?

## Considered Options

- Spring `TaskScheduler` + `CronExpression`, jobs stored in MongoDB and re-registered at startup
- Quartz with a JDBC/MongoDB job store
- JobRunr (MLS-230), MongoDB storage

## Decision Outcome

Chosen option: "Spring `TaskScheduler`", because it ships with Spring Boot (no new dependency), adds
almost no memory, and a single instance needs neither clustering nor a distributed lock.

### Consequences

- Good: no new dependency, no extra collections beyond the jobs and their runs, plain Kotlin code to test.
- Good: a job's state (last run, failure count) lives in our own entity, so the API exposes it directly.
- Bad: runs missed while the app is down are not caught up; the next run follows the cron expression.
- Bad: an App Platform rolling deploy keeps the old and new instances up together for a short while, so a
  job due in that window can run twice (and count one failure twice). Accepted for health pings; a job that
  must not repeat (non-idempotent POST) is not a fit until a lock exists.
- Bad: a second instance would run every job twice. Scaling out needs a lock (ShedLock) or a move to
  JobRunr, which this ADR then supersedes.

## Pros and Cons of the Options

### Quartz

- Good: misfire handling, clustering.
- Bad: heavy dependency and schema, no first-class MongoDB store, more than a single instance needs.

### JobRunr

- Good: MongoDB storage, dashboard, retries (MLS-230).
- Bad: new dependency, background polling and its own dashboard server cost memory on a 512 MB instance.
