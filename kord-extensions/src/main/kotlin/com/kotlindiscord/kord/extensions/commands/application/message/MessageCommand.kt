package com.kotlindiscord.kord.extensions.commands.application.message

import com.kotlindiscord.kord.extensions.CommandException
import com.kotlindiscord.kord.extensions.InvalidCommandException
import com.kotlindiscord.kord.extensions.commands.application.ApplicationCommand
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.sentry.BreadcrumbType
import com.kotlindiscord.kord.extensions.sentry.tag
import com.kotlindiscord.kord.extensions.sentry.user
import com.kotlindiscord.kord.extensions.utils.permissionsForMember
import com.kotlindiscord.kord.extensions.utils.translate
import dev.kord.core.entity.channel.DmChannel
import dev.kord.core.entity.channel.GuildChannel
import dev.kord.core.entity.channel.GuildMessageChannel
import dev.kord.core.event.interaction.MessageCommandInteractionCreateEvent
import io.sentry.Sentry
import mu.KLogger
import mu.KotlinLogging

/** Message context command, for right-click actions on messages. **/
public abstract class MessageCommand<C : MessageCommandContext<*>>(
    extension: Extension
) : ApplicationCommand<MessageCommandInteractionCreateEvent>(extension) {
    private val logger: KLogger = KotlinLogging.logger {}

    /** Command body, to be called when the command is executed. **/
    public lateinit var body: suspend C.() -> Unit

    /** Call this to supply a command [body], to be called when the command is executed. **/
    public fun action(action: suspend C.() -> Unit) {
        body = action
    }

    override fun validate() {
        super.validate()

        if (!::body.isInitialized) {
            throw InvalidCommandException(name, "No command body given.")
        }
    }

    /** Override this to implement your command's calling logic. Check subtypes for examples! **/
    public abstract override suspend fun call(event: MessageCommandInteractionCreateEvent)

    /** Override this to implement a way to respond to the user, regardless of whatever happens. **/
    public abstract suspend fun respondText(context: C, message: String)

    /** Checks whether the bot has the specified required permissions, throwing if it doesn't. **/
    @Throws(CommandException::class)
    public open suspend fun checkBotPerms(context: C) {
        if (context.guild != null) {
            val perms = (context.channel.asChannel() as GuildChannel)
                .permissionsForMember(kord.selfId)

            val missingPerms = requiredPerms.filter { !perms.contains(it) }

            if (missingPerms.isNotEmpty()) {
                throw CommandException(
                    context.translate(
                        "commands.error.missingBotPermissions",
                        null,

                        replacements = arrayOf(
                            missingPerms.map { it.translate(context.getLocale()) }.joinToString(", ")
                        )
                    )
                )
            }
        }
    }

    /** If enabled, adds the initial Sentry breadcrumb to the given context. **/
    public open suspend fun firstSentryBreadcrumb(context: C) {
        if (sentry.enabled) {
            context.sentry.breadcrumb(BreadcrumbType.User) {
                category = "command.application.message"
                message = "Message command \"$name\" called."

                val channel = context.channel.asChannelOrNull()
                val guild = context.guild?.asGuildOrNull()

                data["command"] = name

                if (guildId != null) {
                    data["command.guild"] = guildId!!.asString
                }

                if (channel != null) {
                    data["channel"] = when (channel) {
                        is DmChannel -> "Private Message (${channel.id.asString})"
                        is GuildMessageChannel -> "#${channel.name} (${channel.id.asString})"

                        else -> channel.id.asString
                    }
                }

                if (guild != null) {
                    data["guild"] = "${guild.name} (${guild.id.asString})"
                }
            }
        }
    }

    /** A general way to handle errors thrown during the course of a command's execution. **/
    public open suspend fun handleError(context: C, t: Throwable) {
        logger.error(t) { "Error during execution of $name message command (${context.event})" }

        if (sentry.enabled) {
            logger.debug { "Submitting error to sentry." }

            val channel = context.channel
            val author = context.user.asUserOrNull()

            val sentryId = context.sentry.captureException(t, "Message command execution failed.") {
                if (author != null) {
                    user(author)
                }

                tag("private", "false")

                if (channel is DmChannel) {
                    tag("private", "true")
                }

                tag("command", name)
                tag("extension", extension.name)

                Sentry.captureException(t, "Message command execution failed.")
            }

            logger.debug { "Error submitted to Sentry: $sentryId" }

            val errorMessage = if (extension.bot.extensions.containsKey("sentry")) {
                context.translate("commands.error.user.sentry.slash", null, replacements = arrayOf(sentryId))
            } else {
                context.translate("commands.error.user", null)
            }

            respondText(context, errorMessage)
        } else {
            respondText(context, context.translate("commands.error.user", null))
        }
    }
}
