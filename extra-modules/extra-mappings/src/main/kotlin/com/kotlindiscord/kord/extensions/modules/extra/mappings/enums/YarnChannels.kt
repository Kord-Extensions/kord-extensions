package com.kotlindiscord.kord.extensions.modules.extra.mappings.enums

import com.kotlindiscord.kord.extensions.commands.application.slash.converters.ChoiceEnum

/**
 * Enum representing available Yarn channels.
 *
 * @property str String name used for the channel by Linkie
 */
enum class YarnChannels(override val readableName: String) : ChoiceEnum {
    OFFICIAL("official"),
    SNAPSHOT("snapshot"),

    PATCHWORK("patchwork"),
}
