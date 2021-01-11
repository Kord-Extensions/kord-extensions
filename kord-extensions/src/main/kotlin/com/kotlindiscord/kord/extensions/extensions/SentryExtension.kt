package com.kotlindiscord.kord.extensions.extensions

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.commands.converters.coalescedString
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import com.kotlindiscord.kord.extensions.sentry.sentryId
import com.kotlindiscord.kord.extensions.utils.respond
import io.sentry.Sentry
import io.sentry.UserFeedback
import io.sentry.protocol.SentryId

/**
 * Extension providing a feedback command for use with the Sentry integration.
 *
 * Even if you add this extension manually, it won't do anything unless you've set up the Sentry integration.
 */
public class SentryExtension(bot: ExtensibleBot) : Extension(bot) {
    override val name: String = "sentry"

    override suspend fun setup() {
        if (bot.sentry.enabled) {
            slashCommand<FeedbackArgs> {
                name = "feedback"
                description = "Provide feedback on what you were doing when an error occurred."

                arguments { FeedbackArgs() }

                action {
                    if (!bot.sentry.hasEventId(arguments.id)) {
                        followUp(
                            "The Sentry event ID you supplied either doesn't exist, or is not awaiting " +
                                "feedback."
                        )

                        return@action
                    }
                    val feedback = UserFeedback(
                        arguments.id,
                        member.asMember().tag,
                        member.id.asString,
                        arguments.feedback
                    )

                    Sentry.captureUserFeedback(feedback)
                    bot.sentry.removeEventId(arguments.id)

                    followUp(
                        "Thanks for your feedback - we'll use it to improve our bot and fix " +
                            "the error you encountered!"
                    )
                }
            }

            command {
                name = "feedback"
                description = "If you've been given a Sentry ID by the bot, you can submit feedback on what you were" +
                    "doing using this command.\n\n" +

                    "Your feedback should ideally include a description of what you were doing when the error " +
                    "occurred and what you expected to happen, but the text of your feedback is up to you.\n\n" +

                    "**Note:** Feedback is entirely optional, and you shouldn't feel obliged to submit feedback if " +
                    "you don't wish to - if you've been given an event ID, the error has already been submitted!"

                aliases = arrayOf("sentry-feedback")

                signature(::FeedbackArgs)

                action {
                    val parsed = parse(::FeedbackArgs)

                    if (!bot.sentry.hasEventId(parsed.id)) {
                        message.respond(
                            "The Sentry event ID you supplied either doesn't exist, or is not awaiting " +
                                "feedback."
                        )

                        return@action
                    }

                    val author = message.author!!
                    val feedback = UserFeedback(
                        parsed.id,
                        author.tag,
                        author.id.asString,
                        parsed.feedback
                    )

                    Sentry.captureUserFeedback(feedback)
                    bot.sentry.removeEventId(parsed.id)

                    message.respond(
                        "Thanks for your feedback - we'll use it to improve our bot and fix " +
                            "the error you encountered!"
                    )
                }
            }
        }
    }

    /** Arguments for the feedback command. **/
    public class FeedbackArgs : Arguments() {
        /** Sentry event ID. **/
        public val id: SentryId by sentryId("id", "Sentry event ID")

        /** Feedback message to submit to Sentry. **/
        public val feedback: String by coalescedString(
            "feedback",
            "Feedback to send to the developers."
        )
    }
}
