package com.kotlindiscord.kord.extensions.commands.application

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.commands.application.message.MessageCommand
import com.kotlindiscord.kord.extensions.commands.slash.SlashCommand
import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import dev.kord.core.Kord
import dev.kord.core.entity.application.UserCommand
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/** Registry for all Discord application commands. **/
public open class ApplicationCommandRegistry : KoinComponent {
    /** Current instance of the bot. **/
    public open val bot: ExtensibleBot by inject()

    /** Kord instance, backing the ExtensibleBot. **/
    public val kord: Kord by inject()

    /** Translations provider, for retrieving translations. **/
    public val translationsProvider: TranslationsProvider by inject()

    /** Register a message command. **/
    public open suspend fun register(command: MessageCommand<*>) {
        TODO()
    }

    /** Register a slash command. **/
    public open suspend fun register(command: SlashCommand<*>) {
        TODO()
    }

    /** Register a user command. **/
    public open suspend fun register(command: UserCommand) {
        TODO()
    }
}
