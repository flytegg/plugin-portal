# Universal plugin and network management

## Product contract

Ship one JAR for Bukkit, Spigot, Paper, Folia, and Velocity. Local plugin management is free. Paid access enables management across enrolled servers. BungeeCord is outside this release.

Keep one version, one release artifact, and one set of shared services. Platform entrypoints supply lifecycle, command registration, audiences, server metadata, and update handling. Test the same artifact on each supported runtime; Java requirements remain platform-specific.

## Modules

- `core`: local commands, inventory, downloads, entitlement, configuration, and network client.
- `platforms/bukkit`: Bukkit-family adapter and descriptor.
- `platforms/velocity`: Velocity adapter and descriptor.
- `distribution`: the universal shaded JAR.
- `build-logic`: Gradle conventions.

## Network architecture

The API owns account authentication, paid entitlement, network membership, and enrollment. A Cloudflare Worker routes authenticated WebSockets to one SQLite-backed Durable Object per network. Alchemy manages the Worker and its bindings. Every node opens an outbound connection. The proxy and dashboard can initiate operations without routing backend traffic through the proxy.

Each server receives an individual revocable credential. Enrollment codes are short-lived and single-use. Connection grants identify the network, node or user, allowed operations, and expiry. Authorization is enforced on the server, including renewal and revocation. Browser sessions must not expose long-lived node credentials.

The versioned protocol carries inventory and typed operations, never arbitrary console commands. Targets are explicit. Operations have stable IDs and per-node results. Persist operation state before acknowledging acceptance. Nodes record execution results to avoid repeating mutations after reconnect. An interrupted operation with uncertain outcome requires reconciliation, not blind retry.

Skip offline nodes and report them. Do not silently queue future mutations. Resolve compatible artifacts per target, retain file-path restrictions, and report restart requirements. JAR downloads use HTTP rather than the WebSocket relay. Local controls remain available without paid network access.

Use WebSocket hibernation, reconstruct socket identity from attachments, and store durable results separately. Do not write heartbeats to the database or keep idle rooms awake with intervals. Separate local, test, and production state.

## Delivery sequence

1. Extract the shared core and preserve existing Bukkit behavior.
2. Add Velocity local commands and universal packaging.
3. Add platform-aware artifact selection and restart handling.
4. Add enrollment, scoped authentication, and the Durable Object relay.
5. Add paid network commands and dashboard controls.
6. Verify the complete flow with disposable local servers, then update operating documentation.

## Acceptance checks

- The same JAR starts on tested Bukkit-family and Velocity runtimes.
- Local commands work without a license; network mutation requires current paid access.
- Proxy commands do not intercept backend `/pp` commands.
- Enrollment codes cannot be reused; revoked nodes and other accounts cannot access a network.
- Install, update, uninstall, and inventory refresh work on each platform.
- Incompatible platform artifacts are rejected before installation.
- Network operations return individual results, including offline and partial failures.
- Reconnects and repeated operation IDs do not repeat completed file mutations.
- Hibernation and relay restarts preserve identity and durable operation results.
- Existing temporary editor connections have a documented compatibility path.
- Documentation distinguishes local verification from cloud deployment and production verification.

## Work status

This document records the agreed design. Implementation and validation are tracked in the draft PR; this document does not claim completed platform support.
