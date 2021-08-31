package com.kotlindiscord.kord.extensions.commands.application

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.commands.application.message.MessageCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.SlashCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.SlashCommandParser
import com.kotlindiscord.kord.extensions.commands.application.user.UserCommand
import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.event.interaction.MessageCommandInteractionCreateEvent
import dev.kord.core.event.interaction.UserCommandInteractionCreateEvent
import dev.kord.core.firstOrNull
import kotlinx.coroutines.flow.map
import mu.KotlinLogging
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/** Registry for all Discord application commands. **/
public open class ApplicationCommandRegistry : KoinComponent {
    private val logger = KotlinLogging.logger { }

    /** Current instance of the bot. **/
    public open val bot: ExtensibleBot by inject()

    /** Kord instance, backing the ExtensibleBot. **/
    public val kord: Kord by inject()

    /** Command parser to use for slash commands. **/
    public open val argumentParser: SlashCommandParser = SlashCommandParser()

    /** Translations provider, for retrieving translations. **/
    public val translationsProvider: TranslationsProvider by inject()

    /** Mapping of Discord-side command ID to a message command object. **/
    public val messageCommands: MutableMap<Snowflake, MessageCommand<*>> = mutableMapOf()

    /** Mapping of Discord-side command ID to a slash command object. **/
    public val slashCommands: MutableMap<Snowflake, SlashCommand<*, *>> = mutableMapOf()

    /** Mapping of Discord-side command ID to a user command object. **/
    public val userCommands: MutableMap<Snowflake, UserCommand<*>> = mutableMapOf()

    public suspend fun setup() {

    }

    public suspend fun initialRegistration() {
        if (!bot.settings.applicationCommandsBuilder.register) {
            logger.debug {
                "Application command registration is disabled, pairing existing commands with extension commands."
            }
        }

        val commands: MutableList<ApplicationCommand<*>> = mutableListOf()

        bot.extensions.values.forEach {
            commands += it.messageCommands
            commands += it.slashCommands
            commands += it.userCommands
        }

        syncAll(true, commands)
    }

    // region: Untyped sync functions

    /** Register multiple generic application commands. **/
    public open suspend fun syncAll(removeOthers: Boolean = false, commands: List<ApplicationCommand<*>>) {
        val groupedCommands = commands.groupBy { it.guildId }
    }

    /** Register multiple generic application commands. **/
    public open suspend fun sync(
        removeOthers: Boolean = false,
        guildId: Snowflake?,
        commands: List<ApplicationCommand<*>>
    ) {
        // NOTE: Someday, discord will make real i18n possible, we hope...
        val locale = bot.settings.i18nBuilder.defaultLocale

        val guild = if (guildId != null) {
            kord.getGuild(guildId)
                ?: return logger.warn {
                    "Cannot register application commands for guild ID ${guildId.asString}, " +
                        "as it seems to be missing."
                }
        } else {
            null
        }

        // Get guild commands if we're registering them (guild != null), otherwise get global commands
        val registered = guild?.commands?.map { it.name to it.id }
            ?: kord.globalCommands.map { it.name to it.id }

        if (!bot.settings.applicationCommandsBuilder.register) {
            commands.forEach { commandObj ->
                val existingCommand = registered.firstOrNull { it.first == commandObj.getTranslatedName(locale) }

                if (existingCommand != null) {
                    when (commandObj) {
                        is MessageCommand<*> -> messageCommands[existingCommand.second] = commandObj
                        is SlashCommand<*, *> -> slashCommands[existingCommand.second] = commandObj
                        is UserCommand<*> -> userCommands[existingCommand.second] = commandObj
                    }
                }
            }

            return  // We're only syncing them up, there's no other API work to do
        }
    }

    /** Register a generic application command. **/
    public open suspend fun registerGeneric(command: ApplicationCommand<*>) {
        TODO()
    }

    // endregion

    // region: Typed batch registration functions

    /** Register multiple message commands. **/
    public open suspend fun registerAll(vararg commands: MessageCommand<*>) {
        TODO()
    }

    /** Register multiple slash commands. **/
    public open suspend fun registerAll(vararg commands: SlashCommand<*, *>) {
        TODO()
    }

    /** Register multiple user commands. **/
    public open suspend fun registerAll(vararg commands: UserCommand<*>) {
        TODO()
    }

    // endregion

    // region: Typed registration functions

    /** Register a message command. **/
    public open suspend fun register(command: MessageCommand<*>) {
        TODO()
    }

    /** Register a slash command. **/
    public open suspend fun register(command: SlashCommand<*, *>) {
        TODO()
    }

    /** Register a user command. **/
    public open suspend fun register(command: UserCommand<*>) {
        TODO()
    }

    // endregion

    // region: Typed unregistration functions

    /** Unregister a message command. **/
    public open suspend fun unregister(command: MessageCommand<*>) {
        TODO()
    }

    /** Unregister a slash command. **/
    public open suspend fun unregister(command: SlashCommand<*, *>) {
        TODO()
    }

    /** Unregister a user command. **/
    public open suspend fun unregister(command: UserCommand<*>) {
        TODO()
    }

    // endregion

    // region: Event handlers

    public suspend fun handle(event: MessageCommandInteractionCreateEvent) {
        val commandId = event.interaction.invokedCommandId
        val command = messageCommands[commandId]

        command ?: return logger.warn { "Received interaction for unknown message command: ${commandId.asString}" }

        command.call(event)
    }

    public suspend fun handle(event: ChatInputCommandInteractionCreateEvent) {
        val commandId = event.interaction.command.rootId
        val command = slashCommands[commandId]

        command ?: return logger.warn { "Received interaction for unknown slash command: ${commandId.asString}" }

        command.call(event)
    }

    public suspend fun handle(event: UserCommandInteractionCreateEvent) {
        val commandId = event.interaction.invokedCommandId
        val command = userCommands[commandId]

        command ?: return logger.warn { "Received interaction for unknown user command: ${commandId.asString}" }

        command.call(event)
    }

    // endregion
}
