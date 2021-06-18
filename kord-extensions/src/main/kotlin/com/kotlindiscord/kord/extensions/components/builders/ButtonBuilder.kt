@file:OptIn(KordPreview::class)

package com.kotlindiscord.kord.extensions.components.builders

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.commands.slash.AutoAckType
import com.kotlindiscord.kord.extensions.commands.slash.SlashCommandContext
import com.kotlindiscord.kord.extensions.components.Components
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.sentry.SentryAdapter
import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.common.entity.optional.optional
import dev.kord.core.Kord
import dev.kord.core.entity.GuildEmoji
import dev.kord.core.entity.ReactionEmoji
import dev.kord.core.event.interaction.InteractionCreateEvent
import dev.kord.rest.builder.component.ActionRowBuilder
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Abstract class representing a button builder, providing common functionality and properties.
 */
public abstract class ButtonBuilder : KoinComponent {
    /** The [ExtensibleBot] instance that this extension is installed to. **/
    public val bot: ExtensibleBot by inject()

    /** Current Kord instance powering the bot. **/
    public val kord: Kord by inject()

    /** Sentry adapter, for easy access to Sentry functions. **/
    public val sentry: SentryAdapter by inject()

    /** The button's label text. Optional if you've got an emoji **/
    public open var label: String? = null

    /**
     * A partial emoji object, either a guild or Unicode emoji. Optional if you've got a label.
     *
     * @see emoji
     */
    public open var partialEmoji: DiscordPartialEmoji? = null

    /**
     * Automatic ack type, if not following a parent context.
     */
    public open var ackType: AutoAckType? = AutoAckType.EPHEMERAL

    /** Convenience function for setting [partialEmoji] based on a given Unicode emoji. **/
    public fun emoji(unicodeEmoji: String) {
        partialEmoji = DiscordPartialEmoji(
            name = unicodeEmoji
        )
    }

    /** Convenience function for setting [partialEmoji] based on a given guild custom emoji. **/
    public fun emoji(guildEmoji: GuildEmoji) {
        partialEmoji = DiscordPartialEmoji(
            id = guildEmoji.id,
            name = guildEmoji.name,
            animated = guildEmoji.isAnimated.optional()
        )
    }

    /** Convenience function for setting [partialEmoji] based on a given reaction emoji. **/
    public fun emoji(unicodeEmoji: ReactionEmoji.Unicode) {
        partialEmoji = DiscordPartialEmoji(
            name = unicodeEmoji.name
        )
    }

    /** Convenience function for setting [partialEmoji] based on a given reaction emoji. **/
    public fun emoji(guildEmoji: ReactionEmoji.Custom) {
        partialEmoji = DiscordPartialEmoji(
            id = guildEmoji.id,
            name = guildEmoji.name,
            animated = guildEmoji.isAnimated.optional()
        )
    }

    /** Function used to add this button to an action row. **/
    public abstract fun apply(builder: ActionRowBuilder)

    /** Function called to validate the button. Should throw exceptions if something is invalid. **/
    public abstract fun validate()

    /**
     * For interactive button types, called in order to action the button. Throws [UnsupportedOperationException]
     * by default.
     */
    public open suspend fun call(
        components: Components,
        extension: Extension,
        event: InteractionCreateEvent,
        parentContext: SlashCommandContext<*>? = null
    ) {
        throw UnsupportedOperationException("This type of button doesn't support callable actions.")
    }
}
