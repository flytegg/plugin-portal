package gg.flyte.pluginportal.plugin.network

import com.google.gson.JsonObject

/** Enrollment secrets remain in memory until the local console confirms the displayed identity. */
internal class NetworkEnrollment(
    private val platform: () -> String,
    private val request: (String, JsonObject) -> JsonObject,
) {
    private data class Pending(val code: String, val review: EnrollmentReview)
    private var pending: Pending? = null

    @Synchronized fun review(code: String): EnrollmentReview {
        pending = null
        require(Regex("^[A-Za-z0-9_-]{43}$").matches(code)) { "Invalid enrollment code" }
        val response = request("enrollment", NetworkClient.jsonObject("code" to code, "platform" to platform()))
        val review = EnrollmentReview.from(response)
        require(review.platform == platform()) { "Enrollment is for a different server platform" }
        require(review.expiresAt > System.currentTimeMillis() && review.expiresAt <= System.currentTimeMillis() + 16 * 60_000) { "Enrollment expired; create a new link command" }
        pending = Pending(code, review)
        return review
    }

    @Synchronized fun confirm(): JsonObject {
        val current = pending ?: error("Review a link command in this console before confirming")
        pending = null
        check(current.review.expiresAt > System.currentTimeMillis()) { "Enrollment expired; create a new link command" }
        return request("enroll", NetworkClient.jsonObject(
            "code" to current.code,
            "platform" to current.review.platform,
            "confirmation" to current.review.confirmation,
        )).also { response ->
            require(response.get("nodeId").asString == current.review.nodeId && response.get("networkId").asString == current.review.networkId && response.get("role").asString == current.review.role) { "Enrollment identity changed" }
        }
    }

    @Synchronized fun cancel() { pending = null }
}

data class EnrollmentReview(
    val ownerId: String,
    val ownerName: String,
    val networkId: String,
    val networkName: String,
    val nodeId: String,
    val name: String,
    val platform: String,
    val role: String,
    val expiresAt: Long,
    internal val confirmation: String,
) {
    companion object {
        internal fun from(value: JsonObject): EnrollmentReview {
            fun label(key: String, maximum: Int): String = value.get(key).asString.also {
                require(it.length in 1..maximum) { "Invalid enrollment identity" }
            }.filterNot(Char::isISOControl)
            val role = value.get("role").asString.also { require(it in setOf("node", "controller")) }
            val platform = value.get("platform").asString.also { require(it in setOf("bukkit", "velocity")) }
            val networkId = value.get("networkId").asString.also { require(NetworkStateStore.validId(it)) }
            val nodeId = value.get("nodeId").asString.also { require(NetworkStateStore.validId(it)) }
            val confirmation = value.get("confirmation").asString.also { require(Regex("^[a-f0-9]{64}$").matches(it)) }
            return EnrollmentReview(label("ownerId", 120), label("ownerName", 120), networkId,
                label("networkName", 80), nodeId, label("name", 80), platform, role, value.get("expiresAt").asLong, confirmation)
        }
    }
}
