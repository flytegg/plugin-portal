package gg.flyte.pluginportal.plugin.commands.lamp

import gg.flyte.pluginportal.common.commands.lamp.LampExceptionHandler
import revxrsal.commands.Lamp
import revxrsal.commands.bukkit.actor.BukkitCommandActor
import revxrsal.commands.node.ParameterNode
import revxrsal.commands.process.ParameterValidator
import java.io.File

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class SafeFileName

class SafeFileNameValidator: ParameterValidator<BukkitCommandActor, String?> {
    override fun validate(actor: BukkitCommandActor, value: String?, param: ParameterNode<BukkitCommandActor, String?>, lamp: Lamp<BukkitCommandActor>) {
        // Skip validation if no SafeFileName annotation or if value is null
        if (value == null || param.annotations().get(SafeFileName::class.java) == null) return

        if (value.contains(File.separator) || value.contains("../"))
            throw LampExceptionHandler.PortalCommandException("Invalid file name")
    }
}