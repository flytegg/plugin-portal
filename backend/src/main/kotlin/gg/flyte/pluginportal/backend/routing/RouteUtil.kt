package gg.flyte.pluginportal.backend.routing

import gg.flyte.pluginportal.backend.routing.routes.APIVersion
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*

fun Application.authenticatedRoute(
    version: APIVersion = APIVersion.V1,
    route: String,
    block: Route.() -> Unit
) {
    routing {
        route("${version.path}$route") {
            authenticate("auth-bearer") {
                block()
            }
        }
    }
}