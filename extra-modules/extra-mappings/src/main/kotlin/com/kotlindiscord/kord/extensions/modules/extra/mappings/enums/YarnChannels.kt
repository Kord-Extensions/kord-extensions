package com.kotlindiscord.kord.extensions.modules.extra.mappings.enums

/**
 * Enum representing available Yarn channels.
 *
 * @property str String name used for the channel by Linkie
 */
enum class YarnChannels(val str: String) {
    OFFICIAL("official"),
    SNAPSHOT("snapshot"),

    PATCHWORK("patchwork"),
}
