package gg.flyte.backend.routing.routes.v1.recognize

import gg.flyte.backend.routing.routes.v1.plugins.PluginService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.charsets.*
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.URLDecoder
import java.net.URLEncoder

fun Application.configureRecognizeRoutes() {
    recognizeRouting {
        get {
            val hashes = call.parameters["hashes"]!!.decode<HashSet<HashMap<HashType, String>>>()

            println(hashes)

            call.respond(HttpStatusCode.OK, PluginService.getPluginByHashes(hashes).map { it.toDto() })
        }
    }
}

private fun Application.recognizeRouting(block: Route.() -> Unit) {
    routing {
        route("/v1/recognize") {
            block()
        }
    }
}

private inline fun <reified T> String.decode() = Json.decodeFromString<T>(URLDecoder.decode(this, Charsets.UTF_8))
private inline fun <reified T> T.encode() = URLEncoder.encode(Json.encodeToString(this), Charsets.UTF_8)