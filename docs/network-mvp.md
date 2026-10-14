# Network management MVP

Plugin Portal 4.0.0-beta.2 builds one JAR for Bukkit-family servers and Velocity. Local
management is free. An active owner purchase enables hosted network management.
BungeeCord is outside this release. This candidate is not a published release.

## Enroll a network

1. Sign in to the dashboard. Link your MC License key under Purchases, or use your verified purchase.
2. Choose Add server. Enter a name and choose Paper / Bukkit or Velocity.
3. Allow network commands for a trusted controller, normally your proxy.
4. Install the same JAR on every server. Run its link command in the console, review the account/network/role, then run `pp link confirm` (or `pp link cancel`).
5. View status and inventory for all groups in Your servers. Viewing is read-only.
6. Approve remote plugin writes with `pp control enable` in every target console. Use `/ppv servers`, `/ppv list --server lobby`, or `/ppv install LuckPerms MODRINTH --servers lobby,proxy` from the controller.
7. Inspect `/ppv history` and restart affected servers to apply JAR changes.

Use `/pp` on backends and `/ppv` on Velocity. Existing `/pp network`, `/ppv network`,
and `/ppnetwork` commands remain compatible. The proxy leaves backend `/pp` available.
Names resolve within the controller's group; ambiguous names require UUIDs.

Dashboard and controller writes require local approval on every target. Run
`pp control enable` in the backend console or `ppv control enable` in the proxy
console. `RemoteControl.AllowWrites` defaults to false and cannot be changed through
the editor. Linking a purchase or server never grants this approval. The API tags
operation source; the JAR checks approval again before mutation. Disabling or
unlinking clears approval. Approval survives a normal restart. The dashboard
requires explicit target selection and review before sending a change.

A controller credential can read inventories and request changes only on approved
nodes in its own network.
Keep controller access limited to machines you trust.

Enrollment codes expire after 15 minutes and are consumed once. A node stores its
individual credential in `network-node.json` with owner-only filesystem permissions.
The dashboard never receives that credential. Revoke a node in the dashboard to
remove hosted access; `/pp unlink` deletes its local credential. Leaving
alone does not revoke a copied credential. Re-enrollment creates a new node. Pending review is memory only, and confirmation
is bound to the account, network, node, role, platform and expiry reviewed locally.
Changes to that identity require a new review. Exclude node identity files from
server templates. Migrating the old `Dashboard` setting leaves writes disabled.

## Operations and recovery

Each operation names 1–100 unique node UUIDs and has an immutable UUID. Results are
reported per node: pending, executing, succeeded, staged, failed, skipped, or unknown.
Offline nodes are skipped immediately. There is no offline mutation queue.

Replaying the same operation ID and payload returns its existing results; changing
the payload under that ID is rejected. Nodes persist a reservation before changing
files and persist the result afterward. The journal retains up to 1,000 operations
for 30 days. A crash between a filesystem mutation and result persistence is an
unknown outcome. Inspect the node before submitting a new operation ID. The
dashboard's retry control only prepares targets for review; it does not run them.

Updates honor each node's feature switches and blacklist. Exact versions require
compatible catalog metadata and exclude that plugin from future automatic updates.
Manage Plugin Portal itself through its local upgrade command; remote operations
cannot install, update, or uninstall their own running agent.
Downloads resolve separately for Paper and Velocity. JARs use bounded HTTPS
downloads, platform descriptor checks, optional SHA-256 verification, atomic file
replacement, redirect limits, and symbolic-link restrictions. File changes retain
data folders and need a restart; they never hot-load arbitrary plugin code.

Paper uses its update directory. Velocity stages replacements in
`plugins/pluginportal/pending-updates` and applies them at graceful shutdown after
plugin workers stop. A crash retains pending replacements until a later successful
graceful shutdown. Inspect retained failures manually while the proxy is stopped.
Uninstall removes the verified installed and staged copies, including after a
process restart, and refuses ambiguous duplicate plugin identities.

## Architecture and authorization

The API owns account sessions, current entitlement, network membership, enrollment,
and revocation. Every node opens an outbound WebSocket to a Cloudflare Worker. The
Worker routes each network UUID to one SQLite Durable Object. The proxy submits
operations through the API; it does not forward backend traffic. The dashboard
uses authenticated API calls and polls state. JAR bytes bypass the relay.

The API issues single-use WebSocket tickets valid for 90 seconds and scoped socket
sessions lasting at most five minutes. Tickets use Authorization headers, never
URL query parameters. Nodes renew through the API and reconnect with bounded
backoff. Credentials and enrollment codes are stored as hashes on the API. The
API-to-relay secret is private configuration and is never part of the JAR.

Every control request checks the current owner account. Manual entitlements
and purchase credentials are protected from public account updates. The API's
trusted admin and verified marketplace flows own these writes. Local leaving also
fences queued work to its old node identity, so it cannot run after re-enrollment.

Manual entitlement removal
prevents new operations and renewals immediately; existing sessions expire within
five minutes. Marketplace entitlement verification may be cached for ten minutes,
so provider-side changes may take up to fifteen minutes to remove all live access.
Revocation updates the database first and attempts immediate relay disconnect. If
the relay is unavailable, the bounded session expiry remains the fallback. Owners
can still list membership and revoke nodes after their purchase expires; live
inventory and operation data require active access.

The protocol supports catalog inventory, install, update, and uninstall. It has no
remote shell, arbitrary download URL, restart, or configuration operation. A fork
cannot grant itself hosted access without API authorization. Source-available
license terms prohibit entitlement bypass and unauthorized service access; they
do not make client bytecode unmodifiable or prevent an independent implementation.

## Upgrade and validation

The real upgrade runner replaces the released public 3.8.7 JAR with the universal
beta after installing ViaVersion. It verifies tracking, blacklist state, custom
config, telemetry preference, disk/config key compatibility, external settings and
plugin data. Old dashboard approval resets to false. This is a public 3.x binary
upgrade plus legacy state fixtures, not proof from a historical buyer-specific
Premium binary or a live vendor purchase.

```sh
PLUGIN_PORTAL_JAR=/absolute/path/PluginPortal-4.0.0-beta.2.jar \
  bun scripts/smoke-in-place-upgrade.ts 3.8.7 4.0.0-beta.2 \
  --server-jar .cache/mcjars/paper-1.21.11-latest.jar
```

`smoke-run-paper.ts` builds into ignored `build/paper-smoke-artifacts` by default.
With `--server-jar`, `PLUGIN_PORTAL_JAR` validates an already built candidate.
Neither flow overwrites a normal build used by an existing server.

## Bounds, costs, and outages

The MVP caps accounts at 20 networks, networks at 100 nodes, and pending enrollments
at 100. Relay bodies and frames are limited to 256 KiB; inventories contain at most
500 plugins. Socket message rates, enrollment rates, five-minute operation
deadlines, 1,000-operation retention, and a bounded node work queue limit abuse.

The relay uses WebSocket hibernation, socket attachments, and automatic ping/pong.
It does not hold an interval open. Expiry and retention alarms still wake it;
inventory messages, state polling, storage, and control requests consume quota.
SQLite Durable Objects are available on Cloudflare's free plan, but this does not
guarantee free operation at every scale. Check [Cloudflare pricing](https://developers.cloudflare.com/durable-objects/platform/pricing/)
before deployment and monitor requests, storage, alarms, and quota failures.

API or relay outages disable network control. Local tools remain available. Never
compensate for an outage by bypassing entitlement checks or retrying mutations
blindly. Protect node directories and backups as credentials. Configure a trusted
reverse proxy before relying on forwarded IPs for public enrollment rate limits.

## Build, deployment, and evidence

Modules are `core`, `platforms/bukkit`, `platforms/velocity`, `distribution`, and
`build-logic`. Build the final JAR with `./gradlew :distribution:shadowJar`.
The API companion repository contains `apps/network-relay`, its Alchemy definition,
and the deployment runbook at `docs/network-management.md`.

From the sibling API repository, run `bun run smoke:network` after building the JAR
and shared contracts. This creates disposable MongoDB, Redis, Paper 1.21.11, and
Velocity 3.4.0 containers through OrbStack/Docker and a real local Durable Object
runtime. It verifies ownership, free/paid boundaries, enrollment, tickets, inventory,
platform artifact hashes, installs, updates, blacklists, node/relay restart,
deduplication, uninstall with pending updates, and revocation. It uses no production
credentials or databases; it reads public catalog metadata and downloads public
server/plugin artifacts. Failure logs remain in the printed temporary directory.

The same candidate JAR passed Paper and Velocity on Java 21 locally. Bukkit,
Spigot, and Folia adapters compile; this does not claim a complete runtime matrix.
Shared code targets Java 17; each server may require a newer JVM. Cloudflare
provisioning, production API deployment, live hibernation, and marketplace release
are separate deployment checks and have not been performed for this MVP.
