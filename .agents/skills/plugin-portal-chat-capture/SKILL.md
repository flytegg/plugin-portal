---
name: plugin-portal-chat-capture
description: Capture real Plugin Portal command output with the repo's containerized MC Pilot client and share the Minecraft screenshot. Use for in-game chat review, command-output screenshots, and remote visual checks. Do not use a custom chat renderer when the native client can show the state.
---

# Plugin Portal chat capture

Use the repository's MC Pilot container. It runs Paper 1.21.11 and a headless Fabric client named
`Dawsson`; it does not need a host Minecraft login or a Devbox node.

## Capture native chat

Start or refresh the complete test environment from the repository root:

```bash
make mc-pilot-up
```

Run the requested command and take the screenshot. `mct chat command` accepts the command without
the leading slash.

```bash
docker compose -f compose.mc-pilot.yml exec -T mc-pilot mct server exec op Dawsson
docker compose -f compose.mc-pilot.yml exec -T mc-pilot mct chat clear
docker compose -f compose.mc-pilot.yml exec -T mc-pilot mct chat command 'pp help'
sleep 2
docker compose -f compose.mc-pilot.yml exec -T mc-pilot \
  mct screenshot --output /output/plugin-portal-chat.png
```

The host receives the image at `build/mc-pilot/plugin-portal-chat.png`. Inspect that file before
sharing it. Keep captures under `build/mc-pilot`; build output is ignored and should not be committed.

For another command, replace `pp help` and choose a descriptive output name. Prefer the native
capture because it proves Minecraft's real font, wrapping, colors, hover layout, and spacing.

## Share with Devbox files

Upload from the local Mac. Supplying an explicit scope avoids requiring a `devbox.json` in this
repository.

```bash
d files put build/mc-pilot/plugin-portal-chat.png \
  --project plugin-portal \
  --environment local-mac \
  --ttl 30d \
  --json
```

Do not print or read Devbox credentials. The CLI resolves its machine credential itself.

If the installed `d` returns `INPUT_VALIDATION_FAILED` with `invalid_json`, it is using the old raw
upload protocol. The current Devbox CLI source uses the JSON upload protocol expected by the API.
Build it to a temporary directory and use that binary:

```bash
share_bin_dir="$(mktemp -d /tmp/plugin-portal-share.XXXXXX)"
(cd /Users/dawson/projects/dawsson/devbox/go && go build -o "$share_bin_dir/d" .)
"$share_bin_dir/d" files put \
  /Users/dawson/projects/pp-v3/plugin-portal/build/mc-pilot/plugin-portal-chat.png \
  --project plugin-portal \
  --environment local-mac \
  --ttl 30d \
  --json
```

Treat the returned URL as authenticated until an anonymous request proves otherwise. As of
2026-09-03, `/files/<id>` redirects anonymous visitors to Devbox login and the content route returns
`401`. Say this plainly when the user asks for a public link. Do not claim that upload success proves
anonymous access.

Stop the environment only when the user is finished with it:

```bash
make mc-pilot-down
```
