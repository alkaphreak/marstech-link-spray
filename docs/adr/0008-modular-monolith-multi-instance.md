---
status: proposed
date: 2026-11-27
ticket: https://marstech.myjetbrains.com/youtrack/issue/MLS-275
---

# One artifact on several instances, each enabling its own modules, work shared over NATS JetStream

## Context and Problem Statement

sol4.space runs as one App Platform instance with 512 MB (`apps-s-1vcpu-0.5gb`, heap at
`MaxRAMPercentage=45`), and the heap already plateaus at 72-80 % (MLS-238). Native work (OCR,
image compression, PDF) is the heaviest load, and three choices assume a single instance: the
in-process cron scheduler (ADR 0003: a second instance runs every job twice), the in-memory
rate-limit buckets (ADR 0006) and the Caffeine caches. ADR 0007 drew enforced feature
boundaries (`domain/<feature>`) to prepare a move to several instances. How should sol4.space
run on several machines of very different sizes (App Platform, another cloud, AlkaNaz, the
MacBooks) without splitting the code base?

## Considered Options

- One artifact, deployed N times, each instance enabling a static list of modules, work shared
  over NATS JetStream
- Separate services per domain group behind an API gateway (MLS-127)
- Stay on one instance and scale it vertically

## Decision Outcome

Chosen option: "One artifact, deployed N times", because it keeps MLS-97's single application
(one build, one image, one release) while letting a small instance hand its heavy work to a
larger machine, and ADR 0007's packages already give each module its boundary.

This ADR keeps MLS-97's single-application decision and ADR 0007's packages. It replaces ADR
0003's single-instance assumption and ADR 0006's per-instance buckets, step by step (see
Migration).

### Modules

A module is one ADR 0007 domain with its adapters. The deployment lists the modules an instance
enables, `mt.link-spray.modules=shortener,paste,...` (env `MODULES`), chosen by hand for the
host's memory and CPU. A disabled module's beans are not created (`@ConditionalOnModule`
wrapping `@ConditionalOnExpression`). No runtime measurement or rebalancing.

Modules are not independent today: `OcrService.recognize` resolves the API key
(`ApiKeyResolver`, owned by `apikey`) and charges `QuotaPort` before working, and
`CronJobService` needs `ApiKeyResolver` and `MailPort`. Two rules follow:

- **Delegation seam**: the caller resolves the key and charges the quota, then delegates the
  work itself. A delegated call carries the resolved `ApiCaller`, never the raw key, so a
  worker enabling `nativetools` or `document` runs without `apikey` and without reading
  `api_keys`. The web controllers and the MCP tools (`Sol4McpTools`) call the same domain
  port, local or delegated.
- **Base modules**: `apikey`, the rate limit and mail are enabled on every public instance.

### Hosts and traffic

| Host                     | Public web | Role                                                     |
|--------------------------|------------|----------------------------------------------------------|
| App Platform, other cloud | yes       | serves every web and API route, runs its enabled modules |
| AlkaNaz (Docker, MLS-40) | no         | worker: consumes jobs over an outbound NATS connection   |
| MacBook Air / Pro        | no         | worker, available only while the Mac is awake            |

Only cloud instances sit behind the public domain; no port opens on the home network. Every
public instance serves the whole web and API surface, which is light. A request whose module is
not enabled locally is sent as a NATS request-reply to an instance that enables it.

### Bus: NATS JetStream on Synadia Cloud

| Need                        | NATS feature                                              |
|-----------------------------|-----------------------------------------------------------|
| Delegated light call        | request-reply on `sol4.<module>.<operation>.v<N>`, queue group per module |
| Delegated native call (OCR, compression, PDF) | JetStream work queue on the same subject, pull consumer: a worker fetches only while its `NativeWorkPermit` is free, and replies to the caller's inbox |
| Background jobs             | JetStream work-queue stream, one ack per job              |
| Which instance enables what | NATS Service API (`io.nats.service` in jnats): each module registers as a service with its contract versions; `$SRV.PING` lists them |
| Single-run work (cron)      | KV bucket `sol4-leases`, one key, TTL lease               |
| Shared rate-limit buckets   | KV bucket `sol4-ratelimit`, compare-and-set on revision   |

Every instance, workers included, connects outbound to Synadia Cloud with its own credentials,
restricted to the subjects of its enabled modules. The KV subjects count: only instances
enabling `cron` may write `$KV.sol4-leases.>`, only public instances may write
`$KV.sol4-ratelimit.>`, and a worker may consume a module's work queue only for the modules it
enables.

### Module unavailable

Before sending a delegated call, the caller reads the Service API registry (cached for a few
seconds). With no instance enabling the module and contract version, the API answers 503 at
once with `Retry-After`, no request timeout spent. A delegated call that times out also answers
503.

### Single-run work: cron

Only the cron scheduler (ADR 0003) needs a lease. Index creation (`createIndex` at
`ApplicationReadyEvent`) is idempotent and stays on every instance; expired documents are
removed by MongoDB's own TTL monitor, with no application purge.

- **Scheduling**: only the instance holding the `cron` key in `sol4-leases` keeps the in-memory
  schedule. It renews the lease before its TTL; when it stops, another instance enabling `cron`
  takes the lease and loads every stored job, as `start()` does today.
- **Edits**: any public instance serves the cron API. After writing a job to MongoDB it
  publishes `sol4.cron.changed.v1` with the job id; the lease holder reschedules that job. The
  holder also re-reads the stored jobs every few minutes, so a lost event costs a delay, not a
  lost edit.
- **Next run**: `nextRunAt` is computed from the stored 6-field expression
  (`CronExpression.next`), not from the holder's local `futures` map, so every instance answers
  it.
- **Runs**: each run is published to a work-queue stream with the message id
  `<jobId>-<scheduledFireTime>`, the fire time computed from the expression, not the wall clock,
  so two holders around a handover publish the same id and JetStream drops the second inside
  its duplicate window. Delivery stays at-least-once: a consumer that crashes after the webhook
  call and before its ack runs the job again. Webhook targets must tolerate a repeat.

### Rate limiting

Bucket4j keeps its buckets in `sol4-ratelimit` through a subclass of
`AbstractCompareAndSwapBasedProxyManager`
(`io.github.bucket4j.distributed.proxy.generic.compare_and_swap`) written against the KV
revision; Bucket4j ships no NATS backend. The limit stays global across public instances; each
check costs a KV read and a CAS write, more under contention.

### Shared cache

No shared cache server. The only Caffeine cache today is the rate-limit bucket cache
(`AnonymousQuota`), which moves to NATS KV above; per-instance Caffeine stays for anything else.
Pogocache (MLS-94) was considered and set aside: its RESP subset has no `EVAL`, `WATCH` or
`MULTI`, so Bucket4j's Redis backends cannot run on it; it is a single node with no
replication; and it would be one more server that both the cloud instances and the home workers
must reach. Revisit when a measured read path (for example short-link lookups) needs a cache
shared across instances that NATS KV cannot serve fast enough.

### Large payloads

NATS messages are capped at about 1 MB. A job input or result above a threshold (512 KB) goes
to DigitalOcean Spaces through the existing S3 adapter (MLS-203), under a `jobs/` prefix with a
lifecycle rule that deletes objects after one day; the message carries a pre-signed URL.

RustFS (Apache-2.0, S3-compatible, 1.x stable) joins `docker-compose.dev.yml` and
`docker-compose.test.yml` as the local S3 endpoint, pinned to an exact version since it
releases about weekly: the S3 adapter and the payload path then run without DigitalOcean
Spaces. It is not the cloud-to-home exchange store: hosted on AlkaNaz, the App Platform
instances could reach it only through an inbound port at home. It may later hold files
exchanged between home workers on the LAN.

### Data

Every instance may enable any module, so every instance may get MongoDB credentials. Each
collection keeps the single owning module it has today (ADR 0007); no module reads another
module's collection. All instances share the one MongoDB user and URI; an instance enabling
only stateless modules (`nativetools`, `document`) is simply not given it. A roaming MacBook
runs stateless modules only and never holds the URI; a fixed home host (AlkaNaz) may run a
stateful module, which needs an IP allowlist entry for its public IP, decided in step D.

### Mixed versions

A delegated call's subject carries its contract version. A changed contract publishes on `v<N+1>`
while instances still serving `v<N>` keep consuming it; `v<N>` is removed once the registry shows
no instance on it. A late MacBook image keeps serving the versions it knows.

### Failure modes

| Case                                   | Handling                                                    |
|----------------------------------------|-------------------------------------------------------------|
| Mac goes to sleep mid-job              | Native work is pulled, not pushed: a sleeping Mac fetches nothing new, and its un-acked job is redelivered after the consumer's `ack_wait` |
| Instance stops cleanly                 | NATS connection drained on Spring shutdown, so it leaves the registry at once |
| Worker busy (`NativeWorkPermit` taken) | It does not fetch; another worker or the queue keeps the job |
| Call timeout                           | Light calls 5 s; native calls 30 s (a 15 s OCR page plus the Spaces round trips), then 503 |
| Synadia down                           | Local modules keep working; delegated modules answer 503; the rate limit fails open (logged at WARN); health check `/` does not depend on NATS |
| Admin login on two public instances    | Before step E, the CSRF token moves from the HTTP session to a cookie (`CookieCsrfTokenRepository`), or the load balancer gets session affinity |

### Consequences

- Good: one build, one image, one release; a module moves between hosts by editing a
  deployment's module list.
- Good: native work leaves the 512 MB instance for machines with spare memory.
- Good: no inbound port on the home network; workers only dial out.
- Bad: Synadia Cloud becomes a runtime dependency of every write (rate limit) and of every
  delegated call; when it is down, delegated modules answer 503 and the rate limit fails open
  (see Failure modes).
- Bad: a home host running a stateful module stores the full MongoDB credentials.
- Bad: a custom Bucket4j proxy manager, a module condition and the lease code to write and test.

### Known ceilings

- Synadia Cloud free plan (third-party figures, 2026-08, to confirm on synadia.com before step A):
  10 connections, 10 GiB network data a month, 5 GiB storage, about 1 MB messages, possibly
  5 streams. Each KV bucket is a stream, so `sol4-leases`, `sol4-ratelimit` and the cron and
  native work queues take four of five. Each instance holds at least one connection, and the
  shared rate limit adds traffic on each public write. Move to the Starter plan (49 USD a month, 100 connections,
  100 GiB) when either reaches 80 % of its cap.
- The MacBooks sleep: a module enabled only on a Mac is unavailable (503) while it sleeps.

## Migration

One pull request per step, each releasable on its own:

| Step | Change                                                                   | Ticket |
|------|--------------------------------------------------------------------------|--------|
| A    | NATS client, module list and Service API registry; every module still local, behaviour unchanged | MLS-23 |
| B    | Cron lease, `sol4.cron.changed.v1`, `nextRunAt` from the expression; ADR 0003 amended |        |
| C    | Shared rate-limit buckets in NATS KV; ADR 0006 amended                   |        |
| D    | `nativetools` and `document` as pulled work with Spaces payloads (presigner added to the S3 adapter, Spaces made mandatory for workers); database access for home hosts; first worker on AlkaNaz | MLS-40 |
| E    | CSRF token in a cookie, then a second public instance behind the App Platform load balancer |        |

MLS-127 (API gateway) is no longer needed: every public instance serves the whole surface.

## Pros and Cons of the Options

### Separate services per domain group behind an API gateway

- Good: a service can be scaled and released alone.
- Bad: one JVM per service, which does not fit several services in 512 MB instances.
- Bad: several builds, images and releases, a gateway to run, and contracts between services
  for calls that are in-process today.

### Stay on one instance and scale it vertically

- Good: nothing to build; ADR 0003 and 0006 stay true.
- Bad: a larger App Platform instance costs more every month and cannot use the memory already
  paid for on AlkaNaz and the MacBooks.
- Bad: one instance remains a single point of failure.
