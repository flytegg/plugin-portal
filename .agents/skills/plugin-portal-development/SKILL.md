---
name: plugin-portal-development
description: Implement and validate Plugin Portal universal JAR and hosted network changes in this project.
---

# Plugin Portal development

Read repository AGENTS.md. Use the Gradle wrapper and Bun, preserve unrelated work,
and commit logical changes with committer and explicit paths.

Keep shared logic in core; put platform APIs in platforms/bukkit or
platforms/velocity. distribution owns the one public shaded JAR and marketplace
tasks. Keep both descriptors and their versions aligned. Internal Lamp commands
use pp; Velocity exposes ppv and ppnetwork without intercepting backend pp.
Capture the native command label per invocation. Help, hover hints, pagination,
and asynchronous action buttons must retain that alias, including the shorter
ppnetwork path. Do not store the current alias in shared or thread-local state.

Use numbered prereleases (4.0.0-beta.N) until the universal release is promoted.
Gradle defaults prereleases to Modrinth beta/alpha and Hangar Snapshot. Never send
them to the stable admin upload route or a stable marketplace channel.

Local tools are free. The hosted API enforces paid ownership, enrollment, and
revocation. Never embed account keys, service secrets, or local premium gates.
Keep production URLs as defaults and local overrides behind pluginportal.dev.

Preserve explicit target UUIDs, immutable operation IDs, durable reservations,
offline skipping, uncertain outcomes, blacklist/feature checks, and staged updates.
Do not add arbitrary remote URLs, shell commands, restarts, or configuration writes.

Compile with ./gradlew :plugin:compileKotlin. Build with
./gradlew :distribution:shadowJar. Run existing focused checks at task boundaries.
For network changes, rebuild sibling API shared contracts then run
bun run smoke:network in ../plugin-portal-api. This uses disposable OrbStack
containers and local Durable Objects. Inspect failures, fix the flow, and rerun.
Never substitute a compile for an actual install/update/restart check.

For requested persistent servers and console access, follow the sibling API's
docs/network-playground.md. Keep the backend private, use modern forwarding,
and add 127.0.0.1:25565 to the matching local Minecraft client.

Use shared Bukkit Adventure styling for command output on both platforms. Keep
chat pages short. Append bold headings to an unstyled parent so rows do not
inherit bold. Boxed messages default to normal weight; detail lines must never
inherit heading decorations. Keep info cards centered like the Bukkit command,
with gray labels, white values, and colored status. Put long IDs and result messages in hover/copy actions, and
retain full console details. Gate native command discovery as well as execution.
Verify no-permission and authorized player flows through the proxy after restarts.

Keep COMMANDS.md, README, docs/network-mvp.md and hosted command docs synchronized.
Record local, CI, cloud deployment, and production evidence separately.
