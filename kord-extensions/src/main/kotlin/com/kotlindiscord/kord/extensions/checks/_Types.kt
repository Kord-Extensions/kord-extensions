@file:Suppress("Filename")

package com.kotlindiscord.kord.extensions.checks

import dev.kord.core.event.Event

/** Type alias representing a generic check function. **/
public typealias CheckFun = suspend (Event) -> Boolean
