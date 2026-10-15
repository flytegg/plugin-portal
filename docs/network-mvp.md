# Network management MVP

Plugin Portal 4.0.0-beta.2 builds one JAR for Bukkit-family servers and Velocity.
Local management is free. An active owner purchase enables hosted network management.
This release does not support BungeeCord. This candidate is not a published release.

## Enroll a network

1. Open the dashboard with your account.
2. Link your MC License key under Purchases, or use your verified purchase.
3. Choose Add server.
4. Enter a name.
5. Choose Paper / Bukkit or Velocity.
6. Install the same JAR on every server and proxy.
7. Run its link command in the console.
8. Review the account, network, and role.
9. Run `pp link confirm` to approve the link, or `pp link cancel` to cancel it.
10. View the status and inventory for all networks in Your servers.
11. On each server, run `pp control enable` in the console to approve plugin changes from the dashboard and proxy.
12. On each proxy, use `ppv control enable` in the console.
13. From the proxy, use `/ppv servers`, `/ppv list --server lobby`, or `/ppv install LuckPerms MODRINTH --servers lobby,proxy`.
14. Inspect `/ppv history`.
15. Restart affected servers and proxies to apply JAR changes.

Use `/pp` on servers and `/ppv` on Velocity.
Both use the same commands. The proxy leaves the server's `/pp` available.
The platform determines the role. A Velocity proxy can send plugin changes to servers that allow them.
A Paper server reports its plugins.

Run `/pp link` without arguments to see the saved link status.
This status includes the account, network, node ID, role, connection, approval for plugin changes, and restart count.
If a server has no link, the command directs you to Add server in the dashboard.

From a proxy, use `/ppv refresh --servers lobby,proxy` to refresh explicit inventories.
`--server` is an alias. Without targets, the command never selects every server.
Names refer to servers and proxies in the same network.
If names are ambiguous, use UUIDs.

Each target requires local approval for plugin changes from the dashboard and proxy.
One approval covers both sources of plugin changes.
Run `pp control enable` in the server console or `ppv control enable` in the proxy console.
`RemoteControl.AllowWrites` defaults to false. The editor cannot change it.
A purchase link or server link never grants this approval.

The API identifies the operation source. The JAR checks approval again before it changes files.
Disable and unlink clear approval. A normal restart retains approval.
The dashboard requires explicit target selection and review before it sends a change.
Inventory views are read-only.

A proxy credential permits inventory access in its network.
It permits requests for plugin changes only on approved nodes in that network.
Limit proxy control access to machines that you trust.

Enrollment codes expire after 15 minutes. Each code permits one enrollment.
A node stores its individual credential in `network-node.json` with owner-only filesystem permissions.
The dashboard never receives that credential.

To remove hosted access, revoke the node in the dashboard.
To delete its local credential, run `/pp unlink`.
Unlink alone does not revoke a copied credential. Re-enrollment creates a new node.

Plugin Portal keeps an unconfirmed review in memory only.
Confirmation requires the account, network, node, role, platform, and expiry from the local review.
Changes to that identity require a new review.
Exclude node identity files from server templates. They are credentials.
Migration from the old `Dashboard` option leaves plugin changes disabled.

## Operations and recovery

Each operation names 1–100 unique node UUIDs and has a fixed UUID.
The network reports results for each node as `pending`, `executing`, `succeeded`, `staged`, `failed`, `skipped`, or `unknown`.
It skips offline nodes immediately. It does not queue offline plugin changes or automatically retry them.

A repeat request with the same operation ID and payload returns the saved results.
The network rejects a different payload with that ID.
Before nodes change files, they save a reservation. After the change, they save the result.
The journal retains up to 1,000 operations for 30 days.

If a crash occurs after a file change but before the node saves its result, the result is unknown.
Before you submit a new operation ID, inspect the node.
The dashboard's retry control only prepares targets for review. It does not execute the retry.

Updates use each node's feature switches and blacklist.
Exact versions require compatible catalog metadata. They exclude that plugin from future automatic updates.
Manage Plugin Portal itself through its local upgrade command.
Remote operations cannot install, update, or uninstall the Plugin Portal instance that executes them.

Plugin Portal selects downloads separately for Paper and Velocity.
It limits HTTPS downloads and redirects. It checks platform descriptors and optional SHA-256 digests.
It uses atomic file replacement and symbolic-link restrictions.
File changes retain data folders and require a restart.
They never load arbitrary plugin code into the active process.

Paper uses its update directory.
Velocity stages replacements in `plugins/pluginportal/pending-updates`.
At graceful shutdown, it applies them after plugin workers stop.
After a crash, it retains staged replacements until a successful graceful shutdown.
While the proxy is stopped, inspect retained failures manually.

Uninstall removes the verified installed and staged copies, also after a process restart.
It refuses ambiguous duplicate plugin identities.

Inventories report each plugin as `running`, `staged-install`, `staged-update`, or `staged-removal`.
They include `stagedVersion` for a staged replacement.
Removed managed plugins remain in the inventory while the current JVM has them loaded.
`restartRequired` also covers staged files with no identified plugin.
Inventories retain the 500-entry limit. Restart detection checks all entries and staged files.

Local `/pp list` marks staged changes with `Restart to apply`.
`/pp info` and `/pp link` show the number of changes that require a restart.

## Architecture and authorization

The API controls account sessions, current entitlement, network membership, enrollment, and revocation.
Every node opens an outbound WebSocket to a Cloudflare Worker.
The Worker routes each network UUID to one SQLite Durable Object.
The proxy submits operations through the API. It does not forward server traffic.

The dashboard uses authenticated API calls and polls state.
JAR bytes bypass the relay.

The API issues single-use WebSocket tickets valid for 90 seconds.
It limits socket sessions to five minutes and their authorized scope.
Tickets use Authorization headers, never URL query parameters.
Nodes renew through the API. They reconnect with bounded backoff.

The API stores credentials and enrollment codes as hashes.
The API-to-relay secret is private configuration. The JAR never contains it.

Every control request checks the current owner account.
Public account updates cannot change manual entitlements or purchase credentials.
The API's trusted admin and verified marketplace procedures control these changes.
Unlink prevents queued work for the old node identity from execution after re-enrollment.

Manual entitlement removal immediately prevents new operations and renewals.
Active sessions expire within five minutes.
Marketplace entitlement verification can use a cache for ten minutes.
Thus, provider changes can take up to fifteen minutes to remove all live access.

Revocation updates the database first. It then attempts an immediate relay disconnect.
If the relay is unavailable, the session expiry still limits access.
After their purchase expires, owners can still list membership and revoke nodes.
Live inventory and operation data require active access.

The protocol supports catalog inventory, install, update, and uninstall.
It has no remote shell, arbitrary download URL, restart, or configuration operation.
A fork cannot grant itself hosted access without API authorization.
Source-available
license terms prohibit entitlement bypass and unauthorized service access; they
do not make client bytecode unmodifiable or prevent an independent implementation.

## Upgrade and validation

The upgrade runner installs ViaVersion, then replaces the released public 3.8.7 JAR with the universal beta.
It verifies plugin records, blacklist state, custom configuration, telemetry preference, disk/config key compatibility, external configuration, and plugin data.
The old dashboard approval resets to false.

This check uses a public 3.x binary upgrade and legacy state fixtures.
It does not prove compatibility with a historical buyer-specific Premium binary or a live vendor purchase.

```sh
PLUGIN_PORTAL_JAR=/absolute/path/PluginPortal-4.0.0-beta.2.jar \
  bun scripts/smoke-in-place-upgrade.ts 3.8.7 4.0.0-beta.2 \
  --server-jar .cache/mcjars/paper-1.21.11-latest.jar
```

`smoke-run-paper.ts` builds into ignored `build/paper-smoke-artifacts` by default.
With `--server-jar`, `PLUGIN_PORTAL_JAR` validates a candidate that already exists.
Neither procedure overwrites a normal build that a server uses.

## Bounds, costs, and outages

The MVP limits each account to 20 networks and each network to 100 nodes.
It limits unconfirmed enrollments to 100.
Relay bodies and frames have a 256 KiB limit. Inventories contain at most 500 plugins.
Socket message rates, enrollment rates, and five-minute operation deadlines limit abuse.
The 1,000-operation retention limit and bounded node work queue also limit abuse.

The relay uses WebSocket hibernation, socket attachments, and automatic ping/pong.
It does not hold an interval open. Expiry and retention alarms still wake it.
Inventory messages, state polls, storage, and control requests consume quota.

Cloudflare's free plan includes SQLite Durable Objects.
This does not guarantee free operation at every scale.
Before deployment, check [Cloudflare costs](https://developers.cloudflare.com/durable-objects/platform/pricing/).
Monitor requests, storage, alarms, and quota failures.

API or relay outages disable network control. Local tools remain available.
If an outage occurs, do not bypass entitlement checks.
Before a retry, inspect the result of the plugin changes.
Protect node directories and backups. They contain credentials.

Before you use forwarded IPs for public enrollment rate limits, configure a trusted reverse proxy.

## Build, deployment, and evidence

Modules are `core`, `platforms/bukkit`, `platforms/velocity`, `distribution`, and `build-logic`.
Build the final JAR with `./gradlew :distribution:shadowJar`.
The API companion repository contains `apps/network-relay`, its Alchemy definition, and the deployment guide at `docs/network-management.md`.

Build the JAR and shared contracts.
From the companion API repository, run `bun run smoke:network`.
This creates disposable MongoDB, Redis, Paper 1.21.11, and Velocity 3.4.0 containers through OrbStack/Docker.
It also creates a real local Durable Object runtime.

The test verifies ownership, free/paid access, enrollment, tickets, inventory, and platform artifact hashes.
It also checks installs, updates, blacklists, node/relay restart, deduplication, uninstall with staged updates, and revocation.
It uses no production credentials or databases.
It reads public catalog metadata and downloads public server/plugin artifacts.
Failure logs remain in the temporary directory that the test prints.

The same candidate JAR passed local Paper and Velocity checks on Java 21.
Bukkit, Spigot, and Folia adapters compile. These checks do not cover all runtime combinations.
Shared code targets Java 17. Each server can require a newer JVM.

Cloudflare deployment, production API deployment, live hibernation, and marketplace release require separate checks.
These checks remain incomplete for this MVP.
