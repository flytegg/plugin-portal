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

Keep COMMANDS.md, README, docs/network-mvp.md and hosted command docs synchronized.
Record local, CI, cloud deployment, and production evidence separately.
