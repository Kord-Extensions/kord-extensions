package com.kotlindiscord.kord.extensions.extensions

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.KoinAccessor
import org.koin.core.Koin
import org.koin.core.component.KoinComponent

/** [Extension] abstract class implementing [KoinComponent], for access to Koin functions. **/
public abstract class KoinExtension(
    bot: ExtensibleBot,
    koinAccessor: KoinComponent = KoinAccessor(bot)
) : Extension(bot), KoinComponent by koinAccessor {
    /** Quick access to the bot's [Koin] instance. **/
    public val k: Koin get() = bot.koin
}
