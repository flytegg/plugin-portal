# Security Policy

Please do not open public issues for vulnerabilities or leaked credentials.

Report security concerns privately through the official Plugin Portal support channel or directly to the project owner. Include:

- A short summary.
- Affected command, endpoint, or workflow.
- Reproduction steps or proof of impact.
- Logs with API keys, device tokens, websocket tokens, and license keys redacted.

Never include Plugin Portal API keys, Polymart tokens, MCLicense keys, websocket tokens, server addresses, or private support dumps in public issues.


Network access is enforced by the hosted API and scoped relay sessions. Each node
has a revocable credential; controller credentials can manage the whole network.
Do not share enrollment commands or commit `network-node.json`, journals, runtime
folders, or backups. Local command permissions apply even though local tools are
free. Remote operations cannot execute arbitrary console commands or download URLs.
See [network authorization and recovery](docs/network-mvp.md) for expiry windows,
revocation, uncertain outcomes, and the source-available license boundary.
