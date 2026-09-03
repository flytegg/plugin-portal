SHELL := /bin/sh

.PHONY: help test check smoke paper

help:
	@echo "Plugin Portal commands:"
	@echo "  make test   Run the fast MockBukkit test suite"
	@echo "  make check  Run tests and build the plugin"
	@echo "  make smoke  Test a real disposable Paper server and plugin install"
	@echo "  make paper  Start a reusable Paper server against the local API"

test:
	./gradlew test

check:
	./gradlew clean test build

smoke:
	./gradlew test
	./gradlew :plugin:paperSmoke

paper:
	./gradlew :plugin:runServer -PpluginPortalDev=true
