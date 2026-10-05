package gg.flyte.pluginportal.common.commands.lamp

import gg.flyte.pluginportal.common.types.enums.MarketplacePlatform
import revxrsal.commands.autocomplete.SuggestionProvider
import gg.flyte.pluginportal.common.commands.lamp.PortalCommandActor
import revxrsal.commands.exception.InvalidValueException
import revxrsal.commands.node.ExecutionContext
import revxrsal.commands.parameter.ParameterType
import revxrsal.commands.stream.MutableStringStream

class InvalidMarketplaceException(input: String): InvalidValueException(input)

class MarketplacePlatformType: ParameterType<PortalCommandActor, MarketplacePlatform> {
    override fun parse(
        input: MutableStringStream,
        context: ExecutionContext<PortalCommandActor>
    ): MarketplacePlatform {
        return MarketplacePlatform.of(input.readString().uppercase()) ?: throw InvalidMarketplaceException(input.readString())
    }

    override fun defaultSuggestions(): SuggestionProvider<PortalCommandActor> {
        return SuggestionProvider.of(MarketplacePlatform.entries.map { it.toString() })
    }
}