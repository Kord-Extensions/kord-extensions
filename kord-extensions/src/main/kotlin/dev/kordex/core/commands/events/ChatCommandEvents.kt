/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.core.commands.events

import dev.kord.core.event.message.MessageCreateEvent
import dev.kordex.core.ArgumentParsingException
import dev.kordex.core.commands.chat.ChatCommand

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
	override val reason: String,
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
