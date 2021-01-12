package com.kotlindiscord.kord.extensions.commands.slash

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.KoinAccessor
import com.kotlindiscord.kord.extensions.commands.converters.SlashCommandConverter
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import com.kotlindiscord.kord.extensions.sentry.SentryAdapter
import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.Snowflake
import dev.kord.core.SlashCommands
import dev.kord.core.event.interaction.InteractionCreateEvent
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import mu.KLogger
import mu.KotlinLogging
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent

private val logger: KLogger = KotlinLogging.logger {}

/**
 * Class responsible for keeping track of slash commands, registering and executing them.
 *
 * Currently only single-level commands are supported, no command groups or subcommands.
 *
 * @property bot Current instance of the bot.
 */
@OptIn(KoinApiExtension::class, KordPreview::class)
public open class SlashCommandRegistry(
    public open val bot: ExtensibleBot,
    koinAccessor: KoinComponent = KoinAccessor(bot)
) : KoinComponent by koinAccessor {
    /** @suppress **/
    public open val commands: MutableMap<Snowflake?, MutableList<SlashCommand<out Arguments>>> = mutableMapOf()

    /** @suppress **/
    public open val commandMap: MutableMap<Snowflake, SlashCommand<out Arguments>> = mutableMapOf()

    /** @suppress **/
    public open val api: SlashCommands get() = bot.kord.slashCommands

    private val sentry: SentryAdapter by bot.koin.inject()

    /** Register a slash command here, before they're synced to Discord. **/
    public open fun register(command: SlashCommand<out Arguments>, guild: Snowflake? = null): Boolean {
        commands.putIfAbsent(guild, mutableListOf())

        val args = command.arguments?.invoke()
        var lastArgRequired = true  // Start with `true` because required args must come first

        args?.args?.forEach { arg ->
            if (arg.converter !is SlashCommandConverter) {
                error("Argument ${arg.displayName} does not support slash commands.")
            }

            if (arg.converter.required && !lastArgRequired) {
                error("Required arguments must be placed before non-required arguments.")
            }

            lastArgRequired = arg.converter.required
        }

        val exists = commands[guild]!!.any { it.name == command.name }

        if (exists) {
            return false
        }

        commands[guild]!!.add(command)

        return true
    }

    /**
     * Sync all slash commands to Discord, removing unrecognised ones.
     *
     * Note that Discord doesn't let us get a list of guilds we have commands on, so we can't
     * remove commands for guilds the bot isn't present on.
     */
    @Suppress("TooGenericExceptionCaught")  // Better safe than sorry
    public open suspend fun syncAll() {
        logger.info { "Synchronising slash commands. This may take some time." }

        try {
            sync(null)
        } catch (t: Throwable) {
            logger.error(t) { "Failed to sync global slash commands" }
        }

        commands.keys.filterNotNull().forEach {
            try {
                sync(it)
            } catch (t: Throwable) {
                logger.error(t) { "Failed to sync slash commands for guild ID: $it" }
            }
        }
    }

    /** @suppress **/
    public open suspend fun sync(guild: Snowflake?) {
        val guildObj = if (guild != null) {
            val guildObj = bot.kord.getGuild(guild)

            if (guildObj == null) {
                logger.warn { "Cannot register slash commands for guild ID $guild, as it seems to be missing." }
                return
            }

            guildObj
        } else {
            null
        }

        val registered = commands[guild]!!

        val existing = if (guild == null) {
            api.getGlobalApplicationCommands().map { Pair(it.name, it.id) }.toList()
        } else {
            api.getGuildApplicationCommands(guild).map { Pair(it.name, it.id) }.toList()
        }

        val toAdd = registered.filter { r -> existing.all { it.first != r.name } }
        val toUpdate = registered.filter { r -> existing.any { it.first == r.name } }
        val toRemove = existing.filter { e -> registered.all { it.name != e.first } }

        logger.info {
            if (guild == null) {
                "Global slash commands: ${toAdd.size} to add / ${toUpdate.size} to update / ${toRemove.size} to remove"
            } else {
                "Slash commands for guild ${guildObj?.name}: ${toAdd.size} to add / ${toUpdate.size} to update / " +
                    "${toRemove.size} to remove"
            }
        }

        (toAdd + toUpdate).forEach {
            val args = it.arguments?.invoke()

            if (guild == null) {
                logger.debug { "Adding/updating global slash command ${it.name}" }

                val response = api.createGlobalApplicationCommand(it.name, it.description) {
                    if (args != null) {
                        args.args.forEach { arg ->
                            val converter = arg.converter

                            if (converter !is SlashCommandConverter) {
                                error("Argument ${arg.displayName} does not support slash commands.")
                            }

                            if (this.options == null) this.options = mutableListOf()

                            this.options!! += converter.toSlashOption(arg)
                        }
                    }
                }

                commandMap[response.id] = it
            } else {
                logger.debug { "Adding/updating slash command ${it.name} for guild: ${guildObj?.name}" }

                val response = api.createGuildApplicationCommand(guild, it.name, it.description) {
                    if (args != null) {
                        args.args.forEach { arg ->
                            val converter = arg.converter

                            if (converter !is SlashCommandConverter) {
                                error("Argument ${arg.displayName} does not support slash commands.")
                            }

                            if (this.options == null) this.options = mutableListOf()

                            this.options!! += converter.toSlashOption(arg)
                        }
                    }
                }

                commandMap[response.id] = it
            }
        }

        if (guild == null) {
            api.getGlobalApplicationCommands().filter { e -> toRemove.any { it.second == e.id } }
                .toList()
                .forEach {
                    logger.debug { "Removing global slash command ${it.name}" }
                    it.delete()
                }
        } else {
            api.getGuildApplicationCommands(guild).filter { e -> toRemove.any { it.second == e.id } }
                .toList()
                .forEach {
                    logger.debug { "Removing global slash command ${it.name}" }
                    it.delete()
                }
        }

        logger.info {
            if (guild == null) {
                "Finished synchronising global slash commands"
            } else {
                "Finished synchronising slash commands for guild ${guildObj?.name}"
            }
        }
    }

    /** Handle an [InteractionCreateEvent] and try to execute the corresponding command. **/
    public open suspend fun handle(event: InteractionCreateEvent) {
        val commandId = event.interaction.command.id
        val command = commandMap[commandId]

        if (command == null) {
            logger.warn { "Received interaction for unknown slash command: ${commandId.asString}" }
            return
        }

        if (!command.extension.loaded) {
            logger.info { "Ignoring slash command ${command.name} as the extension is unloaded." }
            return
        }

        command.call(event)
    }
}
