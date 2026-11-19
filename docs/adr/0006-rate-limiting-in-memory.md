---
status: accepted
date: 2026-11-18
ticket: https://marstech.myjetbrains.com/youtrack/issue/MLS-161
---

# Per-IP rate limiting with in-memory Bucket4j buckets in a Caffeine cache

## Context and Problem Statement

The public routes that write (short links, pastes, dashboards, shortened sprays) and the abuse form,
which sends a mail on each report, had no limit, API and HTML form alike. OCR and image compression
already had a fixed-window per-IP quota (`AnonymousQuota`) that reset every IP at once. MLS-268 (MCP
tools that write) needs a per-client limit first. The app runs as a single App Platform instance with
512 MB. Where do the counters live, and which algorithm?

## Considered Options

- Bucket4j token buckets in memory, one per IP, held in a bounded Caffeine cache
- The existing fixed window (`AnonymousQuota`), extended to the write routes
- Redis (or Valkey) with Lua scripts or `bucket4j-redis`, as the ticket first described

## Decision Outcome

Chosen option: "Bucket4j in memory", because a single instance needs no shared store, and a token
bucket refilled gradually removes the double burst a fixed window allows at its boundary. The cache
is capped at 10,000 clients per limiter and drops a client after a window of inactivity, so a flood of
spoofed addresses cannot grow the heap. A client is an IPv4 address or an IPv6 /64, since one IPv6
subscriber holds a whole /64.

A shortened spray pays one token per link it creates (each URL plus the spray link), charged in
`SprayServiceImpl` for the API and the form alike, capped at 100 URLs there, so the writes quota must stay
above 101; a request larger than the whole bucket is a 400, not a 429. HEAD counts
like GET on `/api/url-shortener/shorten`, because Spring MVC runs the GET handler for it. A browser gets
the error page with the 429, an API client the JSON body.

Limits are per IP only. A limit per API key would be bypassed by sending a new fake key each call
unless the key is verified first, which costs a MongoDB read per request: it comes with the MCP
authentication of MLS-268.

### Consequences

- Good: two small dependencies (`bucket4j_jdk17-core`, Caffeine managed by Spring Boot), no new service.
- Good: one `HandlerInterceptor` covers API and form routes; OCR and image compression share the same
  `AnonymousQuota`, now a token bucket.
- Good: a 429 carries `Retry-After`.
- Bad: counters reset on restart or deploy, and a second instance would double every limit. Scaling out
  means a shared store (Valkey with `bucket4j-redis`), which supersedes this ADR.
- Good: the client IP comes from `do-connecting-ip`, set by App Platform. Before MLS-161 it came from
  `CF-Connecting-IP` (never reaching the app) then the first `X-Forwarded-For` entry, which App Platform
  passes through from the client: a forged `X-Forwarded-For` changed the IP seen by the app on both
  sol4.space and the `ondigitalocean.app` domain (checked 2026-11-18 on `/api/ip`).
- Bad: the limit trusts App Platform to overwrite a client-sent `do-connecting-ip`; to check once deployed.

## Pros and Cons of the Options

### The existing fixed window

- Good: no dependency.
- Bad: one window for every IP, cleared at once: a client can send twice the quota across the boundary.

### Redis or Valkey

- Good: shared counters across instances.
- Bad: a new managed service and its cost, for one instance; Redis is not in the stack.
