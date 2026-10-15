# Plugin Portal development

- Use the Gradle wrapper and Bun scripts. Keep unrelated dirty files and running sessions.
- Commit logical changes with `committer "message" <exact files>`. Never stage `.` or rewrite history. Push only when authorized.
- Shared platform-neutral code belongs in `core/`; platform APIs belong in `platforms/bukkit/` and `platforms/velocity/`; the one public JAR belongs in `distribution/`. Use `build-logic/` for Gradle conventions.
- Build with `./gradlew :distribution:shadowJar`; compile compatibility alias `:plugin:compileKotlin` checks both platforms. Publishing tasks live under `:distribution`.
- Local management is free. Paid network entitlement, ownership, enrollment, and revocation are enforced by the hosted API. Do not add local premium gates or embedded secrets.
- Network frames are scoped catalog operations. Keep explicit targets, durable operation IDs, offline skipping, restart staging, and blacklist/feature enforcement. Never add arbitrary remote shell, URL, or restart execution.
- Production endpoints are the default. Local URLs require `-Dpluginportal.dev=true`; `pluginportal.apiUrl` only applies in development.
- Never log keys, enrollment codes, node credentials, or WebSocket tickets. Do not commit runtime files or generated artifacts.
- For cross-repo changes, rebuild the API shared contracts first. Use the sibling API's `bun run smoke:network` for real OrbStack flows. Do not use production databases or credentials.
- Keep README, COMMANDS, hosted docs, and network operating guidance consistent. Distinguish tested runtime support from compile support and local validation from cloud deployment.
