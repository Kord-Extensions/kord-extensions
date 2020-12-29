package com.kotlindiscord.kord.extensions.extensions

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.KoinAccessor
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent

/** [Extension] abstract class implementing [KoinComponent], for access to Koin functions. **/
@OptIn(KoinApiExtension::class)
public abstract class KoinExtension(
    bot: ExtensibleBot,
    koinAccessor: KoinComponent = KoinAccessor(bot)
) : Extension(bot), KoinComponent by koinAccessor
