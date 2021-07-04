@file:OptIn(KordPreview::class)
@file:Suppress("Filename")

package com.kotlindiscord.kord.extensions.components

import dev.kord.common.annotation.KordPreview
import dev.kord.core.event.interaction.InteractionCreateEvent

/** Button check function. **/
public typealias ComponentCheckFun = suspend (InteractionCreateEvent) -> Boolean
