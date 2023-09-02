package gg.flyte.pluginPortal.type.data

import gg.flyte.pluginPortal.type.language.Message.toComponent

data class HelpMessage(val message: String, val permission: String, val aliases: List<String>, val description: String, val usage: String) {
    fun toComponent() = message.toComponent()
}