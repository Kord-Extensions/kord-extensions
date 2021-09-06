@file:Suppress("TooGenericExceptionCaught")

package com.kotlindiscord.kord.extensions.commands.application.slash

import com.kotlindiscord.kord.extensions.CommandException
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.interactions.respond
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.entity.interaction.GroupCommand
import dev.kord.core.entity.interaction.SubCommand
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.message.create.EphemeralInteractionResponseCreateBuilder

public typealias InitialEphemeralChatResponseBuilder =
    (suspend EphemeralInteractionResponseCreateBuilder.(ChatInputCommandInteractionCreateEvent) -> Unit)?

/** Ephemeral slash command. **/
public class EphemeralSlashCommand<A : Arguments>(
    extension: Extension,

    public override val arguments: (() -> A)? = null,
    public override val parentCommand: SlashCommand<*, *>? = null,
    public override val parentGroup: SlashGroup? = null
) : SlashCommand<EphemeralSlashCommandContext<A>, A>(extension) {
    /** @suppress Internal guilder **/
    public var initialResponseBuilder: InitialEphemeralChatResponseBuilder = null

    /** Call this tn open with a response, omit it to ack instead. **/
    public fun initialResponse(body: InitialEphemeralChatResponseBuilder) {
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

        try {
            if (!commandObj.runChecks(event)) {
                return
            }
        } catch (e: CommandException) {
            event.interaction.respondEphemeral { content = e.reason }

            return
        }

        commandObj.run(event)
    }

    override suspend fun run(event: ChatInputCommandInteractionCreateEvent) {
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

            if (arguments != null) {
                val args = registry.argumentParser.parse(arguments, context)

                context.populateArgs(args)
            }

            body(context)
        } catch (e: CommandException) {
            respondText(context, e.reason)
        } catch (t: Throwable) {
            handleError(context, t, this)
        }
    }

    override suspend fun respondText(context: EphemeralSlashCommandContext<A>, message: String) {
        context.respond { content = message }
    }
}
