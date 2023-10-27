/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.extensions.impl

import com.kotlindiscord.kord.extensions.builders.ExtensibleBotBuilder
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.coalescingString
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.chatCommand
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.sentry.SentryAdapter
import com.kotlindiscord.kord.extensions.sentry.sentryId
import com.kotlindiscord.kord.extensions.utils.respond
import com.kotlindiscord.kord.extensions.utils.tagOrUsername
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
    public val sentryAdapter: SentryAdapter by inject()

    /** Bot settings. **/
    public val botSettings: ExtensibleBotBuilder by inject()

    /** Sentry extension settings, from the bot builder. **/
    public val sentrySettings: ExtensibleBotBuilder.ExtensionsBuilder.SentryExtensionBuilder =
        botSettings.extensionsBuilder.sentryExtensionBuilder

    @Suppress("StringLiteralDuplication")  // It's the command name
    override suspend fun setup() {
        if (sentryAdapter.enabled) {
            ephemeralSlashCommand(::FeedbackSlashArgs) {
                name = "extensions.sentry.commandName"
                description = "extensions.sentry.commandDescription.short"

                action {
                    if (!sentryAdapter.hasEventId(arguments.id)) {
                        respond {
                            content = translate("extensions.sentry.error.invalidId")
                        }

                        return@action
                    }

                    val feedback = UserFeedback(
                        arguments.id,
                        member!!.asMember().tagOrUsername(),
                        member!!.id.toString(),
                        arguments.feedback
                    )

                    Sentry.captureUserFeedback(feedback)
                    sentryAdapter.removeEventId(arguments.id)

                    respond {
                        content = translate("extensions.sentry.thanks")
                    }
                }
            }

            chatCommand(::FeedbackMessageArgs) {
                name = "extensions.sentry.commandName"
                description = "extensions.sentry.commandDescription.long"

                aliasKey = "extensions.sentry.commandAliases"

                action {
                    if (!sentryAdapter.hasEventId(arguments.id)) {
                        message.respond(
                            translate("extensions.sentry.error.invalidId"),
                            pingInReply = sentrySettings.pingInReply
                        )

                        return@action
                    }

                    val author = message.author!!
                    val feedback = UserFeedback(
                        arguments.id,
                        author.tagOrUsername(),
                        author.id.toString(),
                        arguments.feedback
                    )

                    Sentry.captureUserFeedback(feedback)
                    sentryAdapter.removeEventId(arguments.id)

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
        public val id: SentryId by sentryId {
			name = "id"
			description = "extensions.sentry.arguments.id"
		}

        /** Feedback message to submit to Sentry. **/
        public val feedback: String by coalescingString {
            name = "feedback"
            description = "extensions.sentry.arguments.feedback"
        }
    }

    /** Arguments for the feedback command. **/
    public class FeedbackSlashArgs : Arguments() {
        /** Sentry event ID. **/
        public val id: SentryId by sentryId {
			name = "id"
			description = "extensions.sentry.arguments.id"
		}

			/** Feedback message to submit to Sentry. **/
        public val feedback: String by string {
            name = "feedback"
            description = "extensions.sentry.arguments.feedback"
        }
    }
}
