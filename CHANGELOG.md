# Changelog

## Unreleased

### 4.0.0-beta.1 universal network candidate

- Hide command roots, completions, and help from players without view access.
- Add compact help pages and paged network results with copyable IDs.
- Show the exact beta version and connection immediately in the info command.
- Match the centered Bukkit info card and keep detail lines at normal weight.
- Keep the invoked command alias in help, hover hints, and action buttons.
- Show help for the attempted command when arguments are missing or invalid.
- Package one JAR with shared core and Bukkit and Velocity entrypoints.
- Make all local tools free, including recognition, bulk updates, and the editor.
- Add paid network enrollment and scoped proxy commands for inventory, install,
  update, and uninstall across explicit backend and proxy targets.
- Select artifacts for each platform and stage Velocity updates at shutdown.
- Persist operation results and recover connections without replaying mutations.
- Fix cache removal and uninstalling installed plus pending plugin versions.
- Enforce hosted authorization rather than embedding account keys in universal JARs.
- Publish the architecture, recovery, security, and local integration workflow.

The candidate is built and tested locally on Paper 1.21.11 and Velocity 3.4.0 with
Java 21. It is not published or deployed to production.

### 3.8.9 candidate

Prepared for release. The JAR is not published yet.

- Fix startup and marketplace access on legacy Paper servers, including 1.8.8.
- Add `/pp list --untracked` for local JARs that Plugin Portal does not manage.
- Add chat pagination for lists and search results. Use `--full` for all results.
- Show usable commands and details in the server console.
- Fix command-specific help and install option ordering.
- Honor explicit reinstalls of the current version.
- Explain skipped plugins during bulk updates.
- Update command, setup, and contributor documentation.

Use Java 17 or later, and meet your server's Java requirements. Tests cover Paper
1.8.8 and 1.21.11 with Java 21, and Paper 26.2 and 26.3 beta with Java 25.
The API metadata fix and marketplace refresh for Paper 26.3 are deployed.
Existing Plugin Portal 3.8.7 works with the corrected API without a plugin upgrade.
The 3.8.9 JAR also preserves its saved installation data on upgrade.
