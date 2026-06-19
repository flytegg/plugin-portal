# Plugin Portal Release Notes

This file is the local source of truth for operator-facing release notes until the release pipeline has a public release-notes endpoint.

## Format

Add one section per shipped plugin version:

```md
## 3.8.2

- Release commit: `<git sha>`
- Built artifacts:
  - `PluginPortal-3.8.2.jar`
- Date: `YYYY-MM-DD`

### Added
- ...

### Fixed
- ...

### Notes
- ...
```

## Rules

- Use the release commit SHA that produced the uploaded jars.
- Keep notes user-facing; avoid internal refactor details unless they explain behavior.
- Mention command syntax changes exactly as users should type them.
- If a release has API/backend requirements, call them out in `Notes`.
- Do not put private bucket URLs, license data, or webhook URLs in this file.

## Future Pipeline

The release script should eventually copy the section for the released version into release metadata stored beside the jars. Until then, plugin webhooks should only include release notes when the API already returns a changelog for the update response.

## 3.8.3 Beta

- Release commit: see GitHub tag `v3.8.3`
- Built artifacts:
  - `PluginPortal-3.8.3.jar`
- Date: `2026-06-19`

### Added

- Added the current Plugin Portal source layout to the public repository.
- Added a repo-local release skill for repeatable GitHub, Modrinth, and Hangar publishing.
- Added marketplace beta publishing support for the single `PluginPortal` JAR.

### Fixed

- Fixed compatibility with Leaf/Paper 26.x version strings through the updated command framework dependency.
- Fixed Plugin Portal self-upgrade flow to use the canonical Plugin Portal marketplace entry and release channels.
- Fixed marketplace changelog publishing to use real Markdown notes instead of escaped newline text.

### Notes

- This beta is published through GitHub prerelease, Modrinth beta, and Hangar Snapshot channels.
- This beta is not uploaded to Plugin Portal's stable admin release API, so stable auto-updater users should not receive it unless they explicitly use the beta channel.
- Premium features are included in the same JAR and remain controlled by runtime entitlement.
