package gg.flyte.pluginportal.common.commands.lamp

import java.util.*

/**
    If the feature is controlled by a command, the @EnabledCommand(Features.XXX) annotation can be used above the
    command method to automatically control the usage of that command, no additional registration necessary.

    If more features are required, putting them into this enum will automatically generate them into the config.yml
    and default them to true.
 */
enum class Features {
    INSTALL,
    UPDATE,
    DELETE,
    LIST,
    RECOGNISE,
    IMPORT,
    EXPORT,
    AUTOMATICALLY_UPDATE_PPP;

    companion object {
        private val enabledFeatures = hashMapOf<Features, Boolean>()

        fun load(states: EnumMap<Features, Boolean>) {
            enabledFeatures.clear()
            enabledFeatures.putAll(states)
        }
    }

    fun isEnabled() = enabledFeatures[this] == true
}