/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.testbot.utils

import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.events.EventContext
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.testbot.TEST_SERVER_ID
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.entity.Message
import dev.kord.core.entity.channel.TextChannel
import dev.kord.rest.builder.message.create.MessageCreateBuilder
import dev.kord.rest.builder.message.create.embed
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock

public typealias LogBody = (suspend () -> Any?)?

public suspend fun Extension.logRaw(builder: MessageCreateBuilder.() -> Unit): Message? {
    val channel = kord.getGuild(TEST_SERVER_ID)
        ?.channels
        ?.filter { it is TextChannel }
        ?.first {
            it.name == "test-logs"
        }

    return (channel as? TextChannel)?.createMessage(builder)
}

public suspend fun CommandContext.log(level: LogLevel, body: LogBody = null): Message? {
    if (!level.isEnabled()) {
        return null
    }

    val desc = body?.invoke()?.toString()

    return command.extension.logRaw {
        embed {
            this.color = level.color

            title = "[${level.name}] Command log: $commandName"
            description = desc

            field {
                name = "Extension"
                value = command.extension.name
            }

            timestamp = Clock.System.now()
        }
    }
}

public suspend fun CommandContext.logError(body: LogBody = null): Message? =
    log(LogLevel.ERROR, body)

public suspend fun CommandContext.logWarning(body: LogBody = null): Message? =
    log(LogLevel.WARNING, body)

public suspend fun CommandContext.logInfo(body: LogBody = null): Message? =
    log(LogLevel.INFO, body)

public suspend fun CommandContext.logDebug(body: LogBody = null): Message? =
    log(LogLevel.DEBUG, body)

public suspend fun EventContext<*>.log(
    level: LogLevel,
    body: LogBody = null
): Message? {
    if (!level.isEnabled()) {
        return null
    }

    val desc = body?.invoke()?.toString()
    val eventClass = event::class.simpleName

    return eventHandler.extension.logRaw {
        embed {
            this.color = level.color

            title = "[${level.name}] Event log: $eventClass"
            description = desc

            field {
                name = "Extension"
                value = eventHandler.extension.name
            }

            timestamp = Clock.System.now()
        }
    }
}

public suspend fun EventContext<*>.logError(body: LogBody = null): Message? =
    log(LogLevel.ERROR, body)

public suspend fun EventContext<*>.logWarning(body: LogBody = null): Message? =
    log(LogLevel.WARNING, body)

public suspend fun EventContext<*>.logInfo(body: LogBody = null): Message? =
    log(LogLevel.INFO, body)

public suspend fun EventContext<*>.logDebug(body: LogBody = null): Message? =
    log(LogLevel.DEBUG, body)
