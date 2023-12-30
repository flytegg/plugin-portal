package gg.flyte.backend.base.auth

import io.ktor.server.application.*
import io.ktor.server.auth.*

fun Application.configureSecurity() {
    install(Authentication) {
        bearer("auth-bearer") {
            realm = "Flyte"
            authenticate { bearerTokenCredential ->
//                val userByToken = PluginService.findUserByToken(bearerTokenCredential.token)
//
//                if (userByToken != null) UserIdPrincipal(userByToken.id.toString())
//                else null
                null
            }
        }
    }
}

//suspend fun UserIdPrincipal?.getMongoUser() = PluginService.getUser(this?.name?.toLong()!!)