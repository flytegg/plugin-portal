SHELL := /bin/sh

MC_PILOT_COMPOSE := docker compose -f compose.mc-pilot.yml
PROJECT_VERSION := $(shell sed -n 's/^projectVersion=//p' gradle.properties)

.PHONY: help test check smoke paper mc-pilot-plugin mc-pilot-up mc-pilot-shell mc-pilot-down

help:
	@echo "Plugin Portal commands:"
	@echo "  make test   Run the fast MockBukkit test suite"
	@echo "  make check  Run tests and build the plugin"
	@echo "  make smoke  Test a real disposable Paper server and plugin install"
	@echo "  make paper  Start a reusable Paper server against the local API"
	@echo "  make mc-pilot-up     Build and start the isolated MC Pilot container"
	@echo "  make mc-pilot-shell  Open a shell in the MC Pilot container"
	@echo "  make mc-pilot-down   Stop the MC Pilot container"

test:
	./gradlew test

check:
	./gradlew clean test build

smoke:
	./gradlew test
	./gradlew :plugin:paperSmoke

paper:
	./gradlew :plugin:runServer -PpluginPortalDev=true

mc-pilot-plugin:
	./gradlew :plugin:shadowJar
	mkdir -p build/mc-pilot
	cp out/PluginPortal-$(PROJECT_VERSION).jar build/mc-pilot/PluginPortal.jar

mc-pilot-up: mc-pilot-plugin
	$(MC_PILOT_COMPOSE) up -d --build
	$(MC_PILOT_COMPOSE) exec -T mc-pilot mc-pilot-bootstrap

mc-pilot-shell:
	$(MC_PILOT_COMPOSE) exec mc-pilot sh

mc-pilot-down:
	$(MC_PILOT_COMPOSE) down
