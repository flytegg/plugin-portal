#!/bin/sh
set -eu

project_dir=/home/node/.mct/projects/-workspace

if [ ! -f "$project_dir/project.json" ]; then
  mct init --name plugin-portal >/dev/null
fi

if [ ! -f "$project_dir/paper-1.21.11/instance.json" ]; then
  mct server create paper-1.21.11 --type paper --version 1.21.11 --eula >/dev/null
fi

if [ ! -f /home/node/.mct/clients/fabric-1.21.11/instance.json ]; then
  mct client create fabric-1.21.11 \
    --version 1.21.11 \
    --loader fabric \
    --account Dawsson \
    --headless >/dev/null
fi

node -e '
const fs = require("node:fs");
const path = "/home/node/.mct/projects/-workspace/project.json";
const project = JSON.parse(fs.readFileSync(path, "utf8"));
project.defaultProfile = "1.21.11";
project.profiles["1.21.11"] = {
  server: "paper-1.21.11",
  clients: ["fabric-1.21.11"],
  deployPlugins: ["/output/PluginPortal.jar"]
};
project.screenshot.outputDir = "/output";
fs.writeFileSync(path, `${JSON.stringify(project, null, 2)}\n`);
'

echo "MC Pilot is ready. Run 'make mc-pilot-shell', then 'mct up --eula'."
