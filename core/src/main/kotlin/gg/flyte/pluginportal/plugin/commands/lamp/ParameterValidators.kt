package gg.flyte.pluginportal.plugin.commands.lamp

import gg.flyte.pluginportal.common.commands.lamp.LampExceptionHandler
import revxrsal.commands.Lamp
import gg.flyte.pluginportal.common.commands.lamp.PortalCommandActor
import revxrsal.commands.node.ParameterNode
import revxrsal.commands.process.ParameterValidator
import java.io.File

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class SafeFileName

class SafeFileNameValidator: ParameterValidator<PortalCommandActor, String?> {
    override fun validate(actor: PortalCommandActor, value: String?, param: ParameterNode<PortalCommandActor, String?>, lamp: Lamp<PortalCommandActor>) {
        // Skip validation if no SafeFileName annotation or if value is null
        if (value == null || param.annotations().get(SafeFileName::class.java) == null) return

        if (value.contains(File.separator) || value.contains("../"))
            throw LampExceptionHandler.PortalCommandException("Invalid file name")
    }
}