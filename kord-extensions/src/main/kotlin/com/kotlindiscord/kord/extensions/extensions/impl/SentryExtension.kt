package com.kotlindiscord.kord.extensions.extensions.impl

import com.kotlindiscord.kord.extensions.commands.converters.coalescedString
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.sentry.SentryAdapter
import com.kotlindiscord.kord.extensions.sentry.sentryId
import com.kotlindiscord.kord.extensions.utils.respond
import io.sentry.Sentry
import io.sentry.UserFeedback
import io.sentry.protocol.SentryId
import org.koin.core.component.inject

/**
 * Extension providing a feedback command for use with the Sentry integration.
 *
 * Even if you add this extension manually, it won't do anything unless you've set up the Sentry integration.
 */
public class SentryExtension : Extension() {
    override val name: String = "sentry"

    /** Sentry adapter, for easy access to Sentry functions. **/
    public val sentry: SentryAdapter by inject()

    @Suppress("StringLiteralDuplication")  // It's the command name
    override suspend fun setup() {
        if (sentry.enabled) {
            slashCommand(::FeedbackSlashArgs) {
                name = "extensions.sentry.commandName"
                description = "extensions.sentry.commandDescription.short"

                action {
                    if (!sentry.hasEventId(arguments.id)) {
                        ephemeralFollowUp(
                            translate("extensions.sentry.error.invalidId")
                        )

                        return@action
                    }

                    val feedback = UserFeedback(
                        arguments.id,
                        member!!.asMember().tag,
                        member!!.id.asString,
                        arguments.feedback
                    )

                    Sentry.captureUserFeedback(feedback)
                    sentry.removeEventId(arguments.id)

                    ephemeralFollowUp(
                        translate("extensions.sentry.thanks")
                    )
                }
            }

            command(::FeedbackMessageArgs) {
                name = "extensions.sentry.commandName"
                description = "extensions.sentry.commandDescription.long"

                aliases = arrayOf("extensions.sentry.commandAlias")

                action {
                    if (!sentry.hasEventId(arguments.id)) {
                        message.respond(
                            translate("extensions.sentry.error.invalidId")
                        )

                        return@action
                    }

                    val author = message.author!!
                    val feedback = UserFeedback(
                        arguments.id,
                        author.tag,
                        author.id.asString,
                        arguments.feedback
                    )

                    Sentry.captureUserFeedback(feedback)
                    sentry.removeEventId(arguments.id)

                    message.respond(
                        translate("extensions.sentry.thanks")
                    )
                }
            }
        }
    }

    /** Arguments for the feedback command. **/
    public class FeedbackMessageArgs : Arguments() {
        /** Sentry event ID. **/
        public val id: SentryId by sentryId("id", "extensions.sentry.arguments.id")

        /** Feedback message to submit to Sentry. **/
        public val feedback: String by coalescedString(
            "feedback",
            "extensions.sentry.arguments.feedback"
        )
    }

    /** Arguments for the feedback command. **/
    public class FeedbackSlashArgs : Arguments() {
        // TODO: It's impossible to translate these right now

        /** Sentry event ID. **/
        public val id: SentryId by sentryId("id", "Sentry event ID")

        /** Feedback message to submit to Sentry. **/
        public val feedback: String by coalescedString(
            "feedback",
            "Feedback to send to the developers"
        )
    }
}
