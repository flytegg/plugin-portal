package gg.flyte.pluginPortal.type.extension

import gg.flyte.pluginPortal.type.manager.language.Message.toComponent
import net.kyori.adventure.audience.Audience
import org.bukkit.entity.Player

var prefix = "<bold><gray>[<aqua>PP</aqua>]</gray> <dark_gray>></bold> "

fun Audience.sendSuccess(message: String) = sendMessage("$prefix<green>$message".toComponent())
fun Audience.sendError(message: String) = sendMessage("$prefix<red>$message".toComponent())
fun Audience.sendWarning(message: String) = sendMessage("$prefix<yellow>$message".toComponent())
fun Audience.sendInfo(message: String) = sendMessage("$prefix<gray>$message".toComponent())