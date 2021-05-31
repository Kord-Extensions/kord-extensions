package com.kotlindiscord.kord.extensions.modules.extra.mappings

import com.kotlindiscord.kord.extensions.builders.ExtensibleBotBuilder
import com.kotlindiscord.kord.extensions.modules.extra.mappings.builders.ExtMappingsBuilder

/**
 * Configure the mappings extension and add it to the bot.
 */
fun ExtensibleBotBuilder.ExtensionsBuilder.extMappings(builder: ExtMappingsBuilder.() -> Unit) {
    val obj = ExtMappingsBuilder()

    builder(obj)
    MappingsExtension.configure(obj)

    add(::MappingsExtension)
}
