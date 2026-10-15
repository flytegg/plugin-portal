package gg.flyte.pluginportal.plugin

import masecla.modrinth4j.client.agent.UserAgent
import masecla.modrinth4j.main.ModrinthAPI

private val USER_AGENT = UserAgent.builder()
    .projectName("PluginPortal")
    .projectVersion(PortalApplication.runtime.description.version)
    .contact("hello@flyte.gg")
    .authorUsername("Flyte")
    .build()

val modrinthClient get() = ModrinthAPI.rateLimited(USER_AGENT, "")