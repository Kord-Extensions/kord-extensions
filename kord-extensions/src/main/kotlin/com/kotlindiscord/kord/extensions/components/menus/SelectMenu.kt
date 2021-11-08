package com.kotlindiscord.kord.extensions.components.menus

import com.kotlindiscord.kord.extensions.components.ComponentWithAction
import com.kotlindiscord.kord.extensions.sentry.BreadcrumbType
import com.kotlindiscord.kord.extensions.sentry.tag
import com.kotlindiscord.kord.extensions.sentry.user
import com.kotlindiscord.kord.extensions.types.FailureReason
import com.kotlindiscord.kord.extensions.utils.scheduling.Task
import dev.kord.core.entity.channel.DmChannel
import dev.kord.core.entity.channel.GuildMessageChannel
import dev.kord.core.event.interaction.SelectMenuInteractionCreateEvent
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.component.SelectOptionBuilder
import io.sentry.Sentry
import mu.KLogger
import mu.KotlinLogging

/** Maximum length for an option's description. **/
public const val DESCRIPTION_MAX: Int = 50

/** Maximum length for an option's label. **/
public const val LABEL_MAX: Int = 25

/** Maximum number of options for a menu. **/
public const val OPTIONS_MAX: Int = 25

/** Maximum length for a menu's placeholder. **/
public const val PLACEHOLDER_MAX: Int = 100

/** Maximum length for an option's value. **/
public const val VALUE_MAX: Int = 100

/** Abstract class representing a select (dropdown) menu component. **/
public abstract class SelectMenu<C : SelectMenuContext>(
    timeoutTask: Task?
) : ComponentWithAction<SelectMenuInteractionCreateEvent, C>(timeoutTask) {
    internal val logger: KLogger = KotlinLogging.logger {}

    /** List of options for the user to choose from. **/
    public val options: MutableList<SelectOptionBuilder> = mutableListOf()

    /** The minimum number of choices that the user must make. **/
    public var minimumChoices: Int = 1

    /** The maximum number of choices that the user can make. Set to `null` for no maximum. **/
    public var maximumChoices: Int? = 1

    /** Placeholder text to show before the user has selected any options. **/
    public var placeholder: String? = null

    @Suppress("MagicNumber")  // WHY DO YOU THINK I ASSIGN IT HERE
    override val unitWidth: Int = 5

    /** Add an option to this select menu. **/
    @Suppress("UnnecessaryParentheses")  // Disagrees with IDEA, amusingly.
    public open suspend fun option(label: String, value: String, body: suspend SelectOptionBuilder.() -> Unit = {}) {
        val builder = SelectOptionBuilder(label, value)

        body(builder)

        if ((builder.description?.length ?: 0) > DESCRIPTION_MAX) {
            error("Option descriptions must not be longer than $DESCRIPTION_MAX characters.")
        }

        if (builder.label.length > LABEL_MAX) {
            error("Option labels must not be longer than $LABEL_MAX characters.")
        }

        if (builder.value.length > VALUE_MAX) {
            error("Option values must not be longer than $VALUE_MAX characters.")
        }

        options.add(builder)
    }

    public override fun apply(builder: ActionRowBuilder) {
        if (maximumChoices == null || maximumChoices!! > options.size) {
            maximumChoices = options.size
        }

        builder.selectMenu(id) {
            allowedValues = minimumChoices..maximumChoices!!

            this.options.addAll(this@SelectMenu.options)
            this.placeholder = this@SelectMenu.placeholder
        }
    }

    @Suppress("UnnecessaryParentheses")  // Disagrees with IDEA, amusingly.
    override fun validate() {
        super.validate()

        if (this.options.isEmpty()) {
            error("Menu components must have at least one option.")
        }

        if (this.options.size > OPTIONS_MAX) {
            error("Menu components must not have more than $OPTIONS_MAX options.")
        }

        if ((this.placeholder?.length ?: 0) > PLACEHOLDER_MAX) {
            error("Menu components must not have a placeholder longer than $PLACEHOLDER_MAX characters.")
        }
    }

    /** If enabled, adds the initial Sentry breadcrumb to the given context. **/
    public open suspend fun firstSentryBreadcrumb(context: C, button: SelectMenu<*>) {
        if (sentry.enabled) {
            context.sentry.breadcrumb(BreadcrumbType.User) {
                category = "component.selectMenu"
                message = "Select menu \"${button.id}\" submitted."

                val channel = context.channel.asChannelOrNull()
                val guild = context.guild?.asGuildOrNull()
                val message = context.message

                data["component"] = button.id

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

                if (message != null) {
                    data["message"] = message.id.asString
                }
            }
        }
    }

    /** A general way to handle errors thrown during the course of a select menu action's execution. **/
    public open suspend fun handleError(context: C, t: Throwable, button: SelectMenu<*>) {
        logger.error(t) { "Error during execution of select menu (${button.id}) action (${context.event})" }

        if (sentry.enabled) {
            logger.trace { "Submitting error to sentry." }

            val channel = context.channel
            val author = context.user.asUserOrNull()

            val sentryId = context.sentry.captureException(t, "Select menu action execution failed.") {
                if (author != null) {
                    user(author)
                }

                tag("private", "false")

                if (channel is DmChannel) {
                    tag("private", "true")
                }

                tag("component", button.id)

                Sentry.captureException(t, "Select menu action execution failed.")
            }

            logger.info { "Error submitted to Sentry: $sentryId" }

            val errorMessage = if (bot.extensions.containsKey("sentry")) {
                context.translate("commands.error.user.sentry.slash", null, replacements = arrayOf(sentryId))
            } else {
                context.translate("commands.error.user", null)
            }

            respondText(context, errorMessage, FailureReason.ExecutionError(t))
        } else {
            respondText(
                context,
                context.translate("commands.error.user", null),
                FailureReason.ExecutionError(t)
            )
        }
    }

    /** Override this to implement a way to respond to the user, regardless of whatever happens. **/
    public abstract suspend fun respondText(context: C, message: String, failureType: FailureReason<*>)
}
