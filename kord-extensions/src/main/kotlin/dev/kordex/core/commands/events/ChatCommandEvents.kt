/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.commands.events

import dev.kord.core.event.message.MessageCreateEvent
import dev.kordex.core.ArgumentParsingException
import dev.kordex.core.commands.chat.ChatCommand
import dev.kordex.core.i18n.types.Key

/** Event emitted when a chat command is invoked. **/
public data class ChatCommandInvocationEvent(
	override val command: ChatCommand<*>,
	override val event: MessageCreateEvent,
) : CommandInvocationEvent<ChatCommand<*>, MessageCreateEvent>

/** Event emitted when a chat command invocation succeeds. **/
public data class ChatCommandSucceededEvent(
	override val command: ChatCommand<*>,
	override val event: MessageCreateEvent,
) : CommandSucceededEvent<ChatCommand<*>, MessageCreateEvent>

/** Event emitted when a chat command's checks fail. **/
public data class ChatCommandFailedChecksEvent(
	override val command: ChatCommand<*>,
	override val event: MessageCreateEvent,
	override val reason: Key,
) : CommandFailedChecksEvent<ChatCommand<*>, MessageCreateEvent>

/** Event emitted when a chat command's argument parsing fails. **/
public data class ChatCommandFailedParsingEvent(
	override val command: ChatCommand<*>,
	override val event: MessageCreateEvent,
	override val exception: ArgumentParsingException,
) : CommandFailedParsingEvent<ChatCommand<*>, MessageCreateEvent>

/** Event emitted when a chat command's invocation fails with an exception. **/
public data class ChatCommandFailedWithExceptionEvent(
	override val command: ChatCommand<*>,
	override val event: MessageCreateEvent,
	override val throwable: Throwable,
) : CommandFailedWithExceptionEvent<ChatCommand<*>, MessageCreateEvent>
