@file:Suppress("TooGenericExceptionCaught")

package com.kotlindiscord.kord.extensions.commands.application.slash

import com.kotlindiscord.kord.extensions.ArgumentParsingException
import com.kotlindiscord.kord.extensions.DiscordRelayedException
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.events.*
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.entity.interaction.GroupCommand
import dev.kord.core.entity.interaction.SubCommand
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.message.create.EphemeralInteractionResponseCreateBuilder

public typealias InitialEphemeralSlashResponseBuilder =
    (suspend EphemeralInteractionResponseCreateBuilder.(ChatInputCommandInteractionCreateEvent) -> Unit)?

/** Ephemeral slash command. **/
public class EphemeralSlashCommand<A : Arguments>(
    extension: Extension,

    public override val arguments: (() -> A)? = null,
    public override val parentCommand: SlashCommand<*, *>? = null,
    public override val parentGroup: SlashGroup? = null
) : SlashCommand<EphemeralSlashCommandContext<A>, A>(extension) {
    /** @suppress Internal guilder **/
    public var initialResponseBuilder: InitialEphemeralSlashResponseBuilder = null

    /** Call this to open with a response, omit it to ack instead. **/
    public fun initialResponse(body: InitialEphemeralSlashResponseBuilder) {
        initialResponseBuilder = body
    }

    override suspend fun call(event: ChatInputCommandInteractionCreateEvent) {
        val eventCommand = event.interaction.command

        val commandObj: SlashCommand<*, *> = when (eventCommand) {
            is SubCommand -> {
                val firstSubCommandKey = eventCommand.name

                this.subCommands.firstOrNull { it.name == firstSubCommandKey }
                    ?: error("Unknown subcommand: $firstSubCommandKey")
            }

            is GroupCommand -> {
                val firstEventGroupKey = eventCommand.groupName
                val group = this.groups[firstEventGroupKey] ?: error("Unknown command group: $firstEventGroupKey")
                val firstSubCommandKey = eventCommand.name

                group.subCommands.firstOrNull { it.name == firstSubCommandKey }
                    ?: error("Unknown subcommand: $firstSubCommandKey")
            }

            else -> this
        }

        commandObj.run(event)
    }

    override suspend fun run(event: ChatInputCommandInteractionCreateEvent) {
        emitEventAsync(EphemeralSlashCommandInvocationEvent(this, event))

        try {
            if (!runChecks(event)) {
                emitEventAsync(
                    EphemeralSlashCommandFailedChecksEvent(
                        this,
                        event,
                        "Checks failed without a message."
                    )
                )

                return
            }
        } catch (e: DiscordRelayedException) {
            event.interaction.respondEphemeral { settings.errorResponseBuilder(this, e.reason) }

            emitEventAsync(
                EphemeralSlashCommandFailedChecksEvent(
                    this,
                    event,
                    e.reason
                )
            )

            return
        }

        val response = if (initialResponseBuilder != null) {
            event.interaction.respondEphemeral { initialResponseBuilder!!(event) }
        } else {
            event.interaction.acknowledgeEphemeral()
        }

        val context = EphemeralSlashCommandContext(event, this, response)

        context.populate()

        firstSentryBreadcrumb(context, this)

        try {
            checkBotPerms(context)
        } catch (e: DiscordRelayedException) {
            respondText(context, e.reason)

            emitEventAsync(
                EphemeralSlashCommandFailedChecksEvent(
                    this,
                    event,
                    e.reason
                )
            )

            return
        }
        if (arguments != null) {
            try {
                val args = registry.argumentParser.parse(arguments, context)

                context.populateArgs(args)
            } catch (e: ArgumentParsingException) {
                respondText(context, e.reason)
                emitEventAsync(EphemeralSlashCommandFailedParsingEvent(this, event, e))

                return
            }
        }

        try {
            body(context)
        } catch (t: Throwable) {
            if (t is DiscordRelayedException) {
                respondText(context, t.reason)
            }

            emitEventAsync(EphemeralSlashCommandFailedWithExceptionEvent(this, event, t))
            handleError(context, t, this)

            return
        }

        emitEventAsync(EphemeralSlashCommandSucceededEvent(this, event))
    }

    override suspend fun respondText(context: EphemeralSlashCommandContext<A>, message: String) {
        context.respond { settings.errorResponseBuilder(this, message) }
    }
}
