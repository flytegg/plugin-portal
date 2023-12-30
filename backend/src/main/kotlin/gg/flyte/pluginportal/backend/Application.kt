package gg.flyte.pluginportal.backend

import com.google.gson.GsonBuilder
import gg.flyte.pluginportal.backend.base.auth.configureSecurity
import gg.flyte.pluginportal.backend.base.installContent
import gg.flyte.pluginportal.backend.routing.routes.v1.plugins.configurePluginRoutes
import gg.flyte.pluginportal.backend.routing.routes.v1.recognize.configureRecognizeRoutes
import io.ktor.server.application.*
import io.ktor.server.netty.*

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module() {
    installContent()
    configureSecurity()

    configurePluginRoutes()
    configureRecognizeRoutes()
}

val gson = GsonBuilder()
    .setPrettyPrinting()
    .create()
