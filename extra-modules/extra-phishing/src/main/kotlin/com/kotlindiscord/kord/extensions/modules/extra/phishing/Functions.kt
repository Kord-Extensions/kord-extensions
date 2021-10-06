package com.kotlindiscord.kord.extensions.modules.extra.phishing

import com.kotlindiscord.kord.extensions.builders.ExtensibleBotBuilder

/**
 * Configure the phishing extension and add it to the bot.
 */
inline fun ExtensibleBotBuilder.ExtensionsBuilder.extPhishing(builder: ExtPhishingBuilder.() -> Unit) {
    val settings = ExtPhishingBuilder()

    builder(settings)

    settings.validate()

    add { PhishingExtension(settings) }
}
