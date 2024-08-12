/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.test.bot.utils

import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.entity.Message
import dev.kord.core.entity.channel.TextChannel
import dev.kord.rest.builder.message.create.MessageCreateBuilder
import dev.kord.rest.builder.message.embed
import dev.kordex.core.commands.CommandContext
import dev.kordex.core.events.EventContext
import dev.kordex.core.extensions.Extension
import dev.kordex.test.bot.TEST_SERVER_ID
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock

public typealias LogBody = (suspend () -> Any?)?

public suspend fun Extension.logRaw(builder: MessageCreateBuilder.() -> Unit): Message? {
	val channel = kord.getGuildOrNull(TEST_SERVER_ID)
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
    body: LogBody = null,
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
