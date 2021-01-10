package com.kotlindiscord.kord.extensions.slash_commands

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.KoinAccessor
import com.kotlindiscord.kord.extensions.commands.converters.SlashCommandConverter
import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.Snowflake
import dev.kord.core.SlashCommands
import dev.kord.rest.builder.interaction.BaseApplicationBuilder
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import mu.KLogger
import mu.KotlinLogging
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent

private val logger: KLogger = KotlinLogging.logger {}

@OptIn(KoinApiExtension::class, KordPreview::class)
public open class SlashCommandRegistry(
    public open val bot: ExtensibleBot,
    koinAccessor: KoinComponent = KoinAccessor(bot)
) : KoinComponent by koinAccessor {
    public open val commands: MutableMap<Snowflake?, MutableList<SlashCommand>> = mutableMapOf()  // null means global

    public open val api: SlashCommands get() = bot.kord.slashCommands

    public open fun register(command: SlashCommand, guild: Snowflake? = null): Boolean {
        commands.putIfAbsent(guild, mutableListOf())

        command.arguments?.args?.forEach { arg ->
            if (arg.converter !is SlashCommandConverter) {
                error("Argument ${arg.displayName} does not support slash commands.")
            }
        }

        val exists = commands[guild]!!.any { it.name == command.name }

        if (exists) {
            return false
        }

        commands[guild]!!.add(command)

        return true
    }

    @Suppress("TooGenericExceptionCaught")  // Better safe than sorry
    public open suspend fun syncAll() {
        logger.info { "Synchronising slash commands. This may take some time." }

        try {
            sync(null)
        } catch(t: Throwable) {
            logger.error(t) { "Failed to sync global slash commands" }
        }

        commands.keys.forEach {
            try {
                sync(it)
            } catch (t: Throwable) {
                logger.error(t) { "Failed to sync slash commands for guild ID: $it" }
            }
        }
    }

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

        val existing = when (guild) {
            null -> api.getGlobalApplicationCommands().map { Pair(it.name, it.id) }.toList()
            else -> api.getGuildApplicationCommands(guild).map { Pair(it.name, it.id) }.toList()
        }

        val registered = commands[guild]!!

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
            if (guild == null) {
                logger.debug { "Adding/updating global slash command ${it.name}" }

                api.createGlobalApplicationCommand(it.name, it.description) {
                    if (it.arguments != null) {
                        it.arguments?.args?.forEach { arg ->
                            val converter = arg.converter

                            if (converter !is SlashCommandConverter) {
                                error("Argument ${arg.displayName} does not support slash commands.")
                            }

                           if (this.options == null) this.options = mutableListOf()

                            this.options!! += converter.toSlashOption(arg)
                        }
                    }
                }
            } else {
                logger.debug { "Adding/updating slash command ${it.name} for guild: ${guildObj?.name}" }

                api.createGuildApplicationCommand(guild, it.name, it.description) {
                    if (it.arguments != null) {
                        it.arguments?.args?.forEach { arg ->
                            val converter = arg.converter

                            if (converter !is SlashCommandConverter) {
                                error("Argument ${arg.displayName} does not support slash commands.")
                            }

                            if (this.options == null) this.options = mutableListOf()

                            this.options!! += converter.toSlashOption(arg)
                        }
                    }
                }
            }
        }

        if (guild == null) {
            api.getGlobalApplicationCommands().filter { e ->
                toRemove.any {
                    logger.debug { "Removing global slash command ${it.first}" }

                    it.second == e.id
                }
            }.toList().forEach { it.delete() }
        } else {
            api.getGuildApplicationCommands(guild).filter { e ->

                toRemove.any {
                    logger.debug { "Removing slash command ${it.first} for guild: ${guildObj?.name}" }

                    it.second == e.id
                }
            }.toList().forEach { it.delete() }
        }

        logger.info {
            if (guild == null) {
                "Finished synchronising global slash commands"
            } else {
                "Finished synchronising slash commands for guild ${guildObj?.name}"
            }
        }
    }
}
