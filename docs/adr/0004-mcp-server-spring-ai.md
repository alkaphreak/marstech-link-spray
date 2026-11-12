---
status: accepted
date: 2026-11-12
ticket: https://marstech.myjetbrains.com/youtrack/issue/MLS-266
---

# Read-only sol4.space tools served over MCP by Spring AI, Streamable HTTP, no authentication

## Context and Problem Statement

MLS-249 compared WebMCP (tools declared by the page, run in the browser) with a server-side MCP
server, and kept the server as an experiment: sol4.space tools offered to outside MCP clients (Claude
Code, Copilot, Kiro). Which server, which transport, and does the first batch need authentication?
The app runs as one App Platform instance with 512 MB (MLS-238).

## Considered Options

- Spring AI MCP server starter (WebMVC), `@McpTool` methods on a bean
- The MCP Java SDK alone, tools registered by hand
- WebMCP in the browser (MLS-249)

## Decision Outcome

Chosen option: "Spring AI MCP server starter", because it plugs into the existing Spring MVC app
and its validation, and turns annotated methods into tool schemas with no registration code.
Spring AI 2.0.2, the latest stable release on 2026-11-12. MLS-264 aligns on the same version.

- **Transport**: stateless Streamable HTTP on `/mcp` (`spring.ai.mcp.server.protocol=stateless`).
  The stateful mode keeps one session in memory per `initialize`; with no authentication, a loop
  of anonymous `initialize` calls could fill the 512 MB instance. The read-only tools need no
  session, server-to-client notifications or sampling.
- **First batch**: uuid, random_number, dns_lookup, dns_reverse, ip_lookup, qr_code. No tool writes
  to the database. DNS propagation is left out: it queries every public resolver on each call.
- **Validation**: each tool with a REST request DTO builds it and runs Bean Validation on it;
  `random_number` goes through `RandomNumberServiceImpl`'s own checks, like `GET /api/random`. MCP
  and REST refuse the same inputs. `ip_lookup` requires the address, unlike `GET /api/ip`, which
  falls back to the caller's own, and validates it with `DnsReverseRequest` (same pattern, plus
  `@NotBlank`).
- **Authentication**: none, like the same REST endpoints (`SecurityConfig` permits everything but
  `/admin/**`). The existing API keys (`ApiKeyEntity`) also grant image storage and an S3
  namespace, so they are not reused for MCP.

### Consequences

- Good: one new dependency, one class of thin tool methods over the existing services.
- Good: the tool list and schemas come from the annotations, so they cannot drift from the code.
- Bad: tools that write or cost more (URL shortening, paste, OCR, images) need an MCP-specific
  authentication and a rate limit first; they are out of this batch.
- Bad: anyone can call the tools, as anyone can call the REST endpoints today.
