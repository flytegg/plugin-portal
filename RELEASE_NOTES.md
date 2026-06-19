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
