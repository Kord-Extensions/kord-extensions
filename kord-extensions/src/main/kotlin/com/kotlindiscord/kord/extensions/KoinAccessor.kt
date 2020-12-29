package com.kotlindiscord.kord.extensions

import org.koin.core.Koin
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent

/** Class implementing [KoinComponent], which you can subclass in your own classes if necessary. **/
@OptIn(KoinApiExtension::class)
public open class KoinAccessor(private val bot: ExtensibleBot) : KoinComponent {
    override fun getKoin(): Koin = bot.koin
}
