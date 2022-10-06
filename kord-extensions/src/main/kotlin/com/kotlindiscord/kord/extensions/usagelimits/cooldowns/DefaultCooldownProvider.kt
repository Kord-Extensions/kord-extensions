package com.kotlindiscord.kord.extensions.usagelimits.cooldowns

import com.kotlindiscord.kord.extensions.builders.ExtensibleBotBuilder
import com.kotlindiscord.kord.extensions.commands.events.ChatCommandInvocationEvent
import com.kotlindiscord.kord.extensions.commands.events.SlashCommandInvocationEvent
import com.kotlindiscord.kord.extensions.usagelimits.DiscriminatingContext
import com.kotlindiscord.kord.extensions.utils.getKoin
import kotlin.time.Duration

/** Default [CooldownProvider] implementation, this serves as a usable example. **/
public class DefaultCooldownProvider : CooldownProvider {

    private val settings: ExtensibleBotBuilder by lazy { getKoin().get() }

    /** Fetches/resolves the configured cooldown [Duration] for the given [type] and [context]. **/
    override suspend fun getCooldown(context: DiscriminatingContext, type: CooldownType): Duration {
        return when (context.event) {
            is SlashCommandInvocationEvent ->
                settings.applicationCommandsBuilder.useLimiterBuilder.cooldowns[type]?.invoke(context)
                    ?: Duration.ZERO
            is ChatCommandInvocationEvent ->
                settings.chatCommandsBuilder.useLimiterBuilder.cooldowns[type]?.invoke(context) ?: Duration.ZERO
            else -> Duration.ZERO
        }
    }
}
