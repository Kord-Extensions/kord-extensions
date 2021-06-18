@file:OptIn(KordPreview::class)

package com.kotlindiscord.kord.extensions.components

import com.kotlindiscord.kord.extensions.components.contexts.InteractiveButtonContext
import dev.kord.common.annotation.KordPreview
import dev.kord.core.event.interaction.InteractionCreateEvent

/** Button check function. **/
public typealias ButtonCheckFun = suspend (InteractionCreateEvent) -> Boolean

/** Receiver function used for button actions. **/
public typealias InteractiveButtonAction = suspend InteractiveButtonContext.() -> Unit
