package gg.flyte.pluginportal.plugin.command.lamp

import gg.flyte.pluginportal.common.types.MarketplacePlatform
import revxrsal.commands.bukkit.actor.BukkitCommandActor
import revxrsal.commands.exception.InvalidValueException
import revxrsal.commands.node.ExecutionContext
import revxrsal.commands.parameter.ParameterType
import revxrsal.commands.stream.MutableStringStream

class InvalidMarketplaceException(input: String): InvalidValueException(input)

class MarketplacePlatformType: ParameterType<BukkitCommandActor, MarketplacePlatform> {
    override fun parse(
        input: MutableStringStream,
        context: ExecutionContext<BukkitCommandActor>
    ): MarketplacePlatform {
        return MarketplacePlatform.of(input.readString().uppercase()) ?: throw InvalidMarketplaceException(input.readString())
    }
}