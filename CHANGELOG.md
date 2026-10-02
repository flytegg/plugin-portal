# Changelog

## Unreleased

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
Paper 26.3 requires the API metadata fix and a marketplace refresh before release.
Existing Plugin Portal 3.8.7 works with the corrected API without a plugin upgrade.
The 3.8.9 JAR also preserves its saved installation data on upgrade.
