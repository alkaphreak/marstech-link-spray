# Bot User-Agent patterns (MLS-273)

Vendored data read by `ClasspathBotUserAgentSource` for `BotUserAgents` to keep robots out of `clickCount`. All files are copied unchanged
from upstream with their MIT licence notices (`LICENSE-*`), so a refresh is a plain download; check that
`BotUserAgentsTest` still passes after it.

One deviation from mintlify: its "(native app)" HTTP clients (CFNetwork, Dalvik) are skipped when loading, since
a person opening a short link from an iOS or Android app is a click, not a robot.

| File                         | Upstream                                                                  | Commit     | License |
|------------------------------|---------------------------------------------------------------------------|------------|---------|
| `crawler-user-agents.json`   | https://github.com/monperrus/crawler-user-agents (`crawler-user-agents.json`) | `a727b35` | MIT     |
| `agent-traffic-agents.json`  | https://github.com/mintlify/agent-traffic (`data/agents.json`)            | `ee1bc00`  | MIT     |
| `agent-traffic-http-clients.json` | https://github.com/mintlify/agent-traffic (`data/http-clients.json`) | `ee1bc00`  | MIT     |

Refresh:

```bash
curl -sL -o crawler-user-agents.json https://raw.githubusercontent.com/monperrus/crawler-user-agents/master/crawler-user-agents.json
curl -sL -o agent-traffic-agents.json https://raw.githubusercontent.com/mintlify/agent-traffic/main/data/agents.json
curl -sL -o agent-traffic-http-clients.json https://raw.githubusercontent.com/mintlify/agent-traffic/main/data/http-clients.json
curl -sL -o LICENSE-crawler-user-agents https://raw.githubusercontent.com/monperrus/crawler-user-agents/master/LICENSE
curl -sL -o LICENSE-agent-traffic https://raw.githubusercontent.com/mintlify/agent-traffic/main/LICENSE
```
