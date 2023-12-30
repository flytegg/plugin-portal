package gg.flyte.pluginportal.backend.routing.routes.v1.plugins

import gg.flyte.pluginportal.backend.gson
import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.server.application.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configurePluginRoutes() {

    pluginRouting {

        get<Plugins.Get> { plugin ->
            if (plugin.limit > 25) return@get call.respond(
                HttpStatusCode.BadRequest,
                "Limit cannot be greater than 25"
            )


            call.respond(
                HttpStatusCode.OK,
                PluginService.getPaginatedResultFromDatabase(
                    plugin.limit,
                    plugin.offset,
                    plugin.name
                )
            )
        }

        get<Plugins.Id> { plugin ->
            val idPlugin = PluginService.getPlugin(plugin.id) ?: return@get call.respond(
                HttpStatusCode.NotFound,
                "Plugin not found"
            )

            call.respond(HttpStatusCode.OK, idPlugin.toDto())
        }

    }
}

@Resource("/v1/plugins")
class Plugins {

    @Resource("")
    class Get(
        val limit: Int = 1,
        val offset: Int = 0,
        val name: String = "",
    )

    @Resource("{id}")
    class Id(val id: String)

}

private fun Application.pluginRouting(block: Route.() -> Unit) {
    routing {
        rateLimit(RateLimitName("public")) {
            route("/v1/plugins") {
                block()
            }
        }
    }
}