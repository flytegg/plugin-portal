# Local Crafty panel

This repository includes a helper for a local Crafty Controller panel.
Use the web panel to inspect Plugin Portal with a console, file manager, logs, and server controls.

The helper keeps all local state under `.crafty/`. Git ignores this directory.
The helper does not require the closed-source Plugin Portal API/web repository.

Requirements:

- Docker with Compose
- Bun
- `curl`
- `zip`

## Start the panel

```bash
./panel.ts up
```

`up` starts Crafty and creates server import archives.
It uses the generated local admin credentials to authenticate.
It creates Crafty server records through Crafty's v2 API.

Open this URL:

```text
https://localhost:8443
```

The container logs show the initial Crafty credentials:

```bash
./panel.ts logs
```

At first startup, Crafty also writes the generated admin credentials to this file:

```text
.crafty/config/default-creds.txt
```

## Automatic server creation

By default, the helper builds the current Plugin Portal JAR.
It creates Crafty servers for these versions:

- Paper `1.21.11`
- Leaf `26.2` build `2`

The helper mounts the source import folders in Crafty at these paths:

```text
/crafty/import/paper-1.21.11
/crafty/import/leaf-26.2
```

The helper packages those folders into `/crafty/import/upload/<profile>.zip`.
It calls this endpoint:

```text
POST /api/v2/servers
```

Each created server uses:

```text
Executable: server.jar
Command: java -Xms1024M -Xmx2048M -jar server.jar nogui
Ports: 25500, 25501, ...
```

Each server includes:

- `server.jar`
- `eula.txt`
- `server.properties`
- `plugins/PluginPortal-<version>.jar`

## Commands

```bash
# Create files without a Crafty start
./panel.ts setup

# Start Crafty with the default Paper/Leaf servers and a JAR that already exists
./panel.ts up --skip-build

# Start Crafty without new server records
./panel.ts up --no-create

# Create or update every available runtime profile in Crafty
./panel.ts provision --all

# Start the default Paper/Leaf servers through Crafty with local EULA acceptance
./panel.ts start

# Print recent Minecraft server logs
./panel.ts server-logs

# Create import archives only
./panel.ts seed --all

# Add extra Crafty server profiles
./panel.ts provision --profile purpur-1.21.11 --profile folia-1.21.11

# Show container status
./panel.ts status

# Stop Crafty
./panel.ts down

# Delete local Crafty data
./panel.ts clean
```

## Profiles

- `paper-1.21.11`
- `leaf-26.2`
- `purpur-1.21.11`
- `folia-1.21.11`
- `spigot-1.21.11`

## Notes

The helper does not write directly into Crafty's internal database.
It uses Crafty's supported v2 API to create servers.
It skips servers with names that already exist.

Before it starts a server, the helper writes `eula.txt` with `eula=true`.
If you accept the Minecraft EULA, use this only for local test servers.

Crafty's official Docker image and docs use HTTPS port `8443`, Dynmap port `8123`, and Bedrock UDP `19132`.
They also use a range of Minecraft server ports.
This helper maps host ports `25500-25600` by default.
