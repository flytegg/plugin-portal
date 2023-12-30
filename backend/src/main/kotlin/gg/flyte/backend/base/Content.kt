package gg.flyte.backend.base

import io.ktor.serialization.gson.*
import io.ktor.server.application.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import org.slf4j.event.*
import kotlin.time.Duration.Companion.seconds

fun Application.installContent() {
    install(Resources)

    install(ContentNegotiation) {
        gson {
            setPrettyPrinting()
        }
    }

    install(RateLimit) {
        register(RateLimitName("public")) {
            rateLimiter(limit = 100, refillPeriod = 60.seconds)
        }
        register(RateLimitName("protected")) {
            rateLimiter(limit = 250, refillPeriod = 60.seconds)
        }
    }
}
