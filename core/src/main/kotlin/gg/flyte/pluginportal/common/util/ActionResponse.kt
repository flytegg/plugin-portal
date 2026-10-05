package gg.flyte.pluginportal.common.util

import gg.flyte.pluginportal.common.chat.sendFailure
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component

interface ActionResponse<T> {
    val success: Boolean
    val meta: T?

    fun alertFailure(audience: Audience)
}

class ActionResponseString<T>(override val success: Boolean, val error: String?, override val meta: T? = null): ActionResponse<T> {
    override fun alertFailure(audience: Audience) { if (!success && error != null) audience.sendFailure(error) }
}

class ActionResponseComponent<T>(override val success: Boolean, val error: Component?, override val meta: T? = null): ActionResponse<T> {
    override fun alertFailure(audience: Audience) { if (!success && error != null) audience.sendMessage(error) }
}