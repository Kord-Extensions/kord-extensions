/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

@file:Suppress("TooGenericExceptionCaught")

package com.kotlindiscord.kord.extensions.commands.application.slash

import com.kotlindiscord.kord.extensions.ArgumentParsingException
import com.kotlindiscord.kord.extensions.DiscordRelayedException
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.events.*
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.types.FailureReason
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.message.create.InteractionResponseCreateBuilder

public typealias InitialEphemeralSlashResponseBuilder =
    (suspend InteractionResponseCreateBuilder.(ChatInputCommandInteractionCreateEvent) -> Unit)?

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
        findCommand(event).run(event)
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
            event.interaction.respondEphemeral {
                settings.failureResponseBuilder(this, e.reason, FailureReason.ProvidedCheckFailure(e))
            }

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
            event.interaction.deferEphemeralMessage()
        }

        val context = EphemeralSlashCommandContext(event, this, response)

        context.populate()

        firstSentryBreadcrumb(context, this)

        try {
            checkBotPerms(context)
        } catch (e: DiscordRelayedException) {
            respondText(context, e.reason, FailureReason.OwnPermissionsCheckFailure(e))

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
                respondText(context, e.reason, FailureReason.ArgumentParsingFailure(e))
                emitEventAsync(EphemeralSlashCommandFailedParsingEvent(this, event, e))

                return
            }
        }

        try {
            body(context)
        } catch (t: Throwable) {
            emitEventAsync(EphemeralSlashCommandFailedWithExceptionEvent(this, event, t))

            if (t is DiscordRelayedException) {
                respondText(context, t.reason, FailureReason.RelayedFailure(t))

                return
            }

            handleError(context, t, this)

            return
        }

        emitEventAsync(EphemeralSlashCommandSucceededEvent(this, event))
    }

    override suspend fun respondText(
        context: EphemeralSlashCommandContext<A>,
        message: String,
        failureType: FailureReason<*>
    ) {
        context.respond { settings.failureResponseBuilder(this, message, failureType) }
    }
}
