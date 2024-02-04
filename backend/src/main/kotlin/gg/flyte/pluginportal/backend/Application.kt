package gg.flyte.pluginportal.backend

import ch.qos.logback.classic.spi.LogbackServiceProvider
import com.google.gson.GsonBuilder
import gg.flyte.pluginportal.backend.base.auth.configureSecurity
import gg.flyte.pluginportal.backend.base.installContent
import gg.flyte.pluginportal.backend.routing.routes.v1.plugins.configurePluginRoutes
import gg.flyte.pluginportal.backend.routing.routes.v1.recognize.configureRecognizeRoutes
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun main() {
    embeddedServer(
        Netty,
        port = 5006,
        module = Application::module
    ).start(wait = true)
}

fun Application.module() {
    installContent()
    configureSecurity()

    configurePluginRoutes()
    configureRecognizeRoutes()
}

val gson = GsonBuilder()
    .setPrettyPrinting()
    .create()
