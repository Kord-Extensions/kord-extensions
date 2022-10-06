package com.kotlindiscord.kord.extensions.usagelimits.ratelimits

import com.kotlindiscord.kord.extensions.builders.ExtensibleBotBuilder
import com.kotlindiscord.kord.extensions.commands.events.ChatCommandInvocationEvent
import com.kotlindiscord.kord.extensions.commands.events.SlashCommandInvocationEvent
import com.kotlindiscord.kord.extensions.usagelimits.CachedUsageLimitType
import com.kotlindiscord.kord.extensions.usagelimits.DiscriminatingContext
import com.kotlindiscord.kord.extensions.utils.getKoin

/** Default [RateLimitProvider] implementation, this serves as a usable example. **/
public class DefaultRateLimitProvider : RateLimitProvider {

    private val settings: ExtensibleBotBuilder by lazy { getKoin().get()  }

    public override suspend fun getRateLimit(context: DiscriminatingContext, type: CachedUsageLimitType): RateLimit {
        return when (context.event) {
            is SlashCommandInvocationEvent ->
                settings.applicationCommandsBuilder.useLimiterBuilder.rateLimits[type]?.invoke(context)
                    ?: RateLimit.disabled()
            is ChatCommandInvocationEvent ->
                settings.chatCommandsBuilder.useLimiterBuilder.rateLimits[type]?.invoke(context) ?: RateLimit.disabled()
            else -> RateLimit.disabled()
        }
    }
}
