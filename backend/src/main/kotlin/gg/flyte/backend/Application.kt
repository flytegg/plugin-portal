package gg.flyte.backend

import com.google.gson.GsonBuilder
import gg.flyte.backend.base.auth.configureSecurity
import gg.flyte.backend.base.installContent
import gg.flyte.backend.routing.routes.v1.plugins.configurePluginRoutes
import gg.flyte.backend.routing.routes.v1.recognize.configureRecognizeRoutes
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
