# Local Crafty Panel

This repo includes a local Crafty Controller setup helper for inspecting Plugin Portal in a Minecraft-hosting-style web panel with console, file manager, logs, and server controls.

The helper keeps all local state under `.crafty/`, which is ignored by git. The closed-source Plugin Portal API/web repo is not required.

Requirements:

- Docker with Compose
- Bun
- `curl`
- `zip`

## Start The Panel

```bash
./panel.ts up
```

`up` starts Crafty, seeds server import archives, logs in with the generated local admin credentials, and creates Crafty server records through Crafty's v2 API.

Open:

```text
https://localhost:8443
```

The initial Crafty login is printed in container logs:

```bash
./panel.ts logs
```

On first boot, Crafty also writes the generated admin login to:

```text
.crafty/config/default-creds.txt
```

## Auto-Created Servers

By default, the helper builds the current Plugin Portal jar and creates Crafty servers for:

- Paper `1.21.11`
- Leaf `26.2` build `2`

The source import folders are mounted into Crafty at:

```text
/crafty/import/paper-1.21.11
/crafty/import/leaf-26.2
```

The helper packages those folders into `/crafty/import/upload/<profile>.zip` and calls:

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

## Useful Commands

```bash
# Setup files only, without starting Crafty
./panel.ts setup

# Start Crafty and auto-create default Paper/Leaf servers using an already-built jar
./panel.ts up --skip-build

# Start Crafty without creating server records
./panel.ts up --no-create

# Create/update every available runtime profile in Crafty
./panel.ts provision --all

# Accept the EULA locally and start the default Paper/Leaf servers through Crafty
./panel.ts start

# Print recent Minecraft server logs
./panel.ts server-logs

# Seed import archives only
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

The helper does not write directly into Crafty's internal database. It uses Crafty's supported v2 API for server creation and skips existing servers by name.

The helper writes `eula.txt` with `eula=true` before starting a server. Only use this for local test servers if you accept the Minecraft EULA.

Crafty's official Docker image and docs use HTTPS port `8443`, Dynmap port `8123`, Bedrock UDP `19132`, and a Minecraft server port range. This helper maps host ports `25500-25600` by default.
