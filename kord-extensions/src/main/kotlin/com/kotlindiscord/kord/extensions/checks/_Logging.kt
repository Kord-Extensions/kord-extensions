package com.kotlindiscord.kord.extensions.checks

import dev.kord.core.event.Event
import mu.KLogger

internal inline fun KLogger.failed(reason: String) =
    debug { "Failing check: $reason" }

internal inline fun KLogger.passed() =
    debug { "Passing check." }

internal inline fun KLogger.passed(reason: String) =
    debug { "Passing check: $reason" }

internal inline fun KLogger.nullChannel(event: Event) =
    debug { "Channel for event $event is null. This type of event may not be supported." }

internal inline fun KLogger.nullGuild(event: Event) =
    debug { "Guild for event $event is null. This type of event may not be supported." }

internal inline fun KLogger.nullMember(event: Event) =
    debug { "Member for event $event is null. This type of event may not be supported." }
