/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.core.commands.events

import dev.kord.core.event.interaction.MessageCommandInteractionCreateEvent
import dev.kordex.core.commands.application.message.EphemeralMessageCommand
import dev.kordex.core.commands.application.message.MessageCommand
import dev.kordex.core.commands.application.message.PublicMessageCommand

// region Invocation events

/** Basic event emitted when a message command is invoked. **/
public interface MessageCommandInvocationEvent<C : MessageCommand<*, *>> :
	CommandInvocationEvent<C, MessageCommandInteractionCreateEvent>

/** Event emitted when an ephemeral message command is invoked. **/
public data class EphemeralMessageCommandInvocationEvent(
	override val command: EphemeralMessageCommand<*>,
	override val event: MessageCommandInteractionCreateEvent,
) : MessageCommandInvocationEvent<EphemeralMessageCommand<*>>

/** Event emitted when a public message command is invoked. **/
public data class PublicMessageCommandInvocationEvent(
	override val command: PublicMessageCommand<*>,
	override val event: MessageCommandInteractionCreateEvent,
) : MessageCommandInvocationEvent<PublicMessageCommand<*>>

// endregion

// region Succeeded events

/** Basic event emitted when a message command invocation succeeds. **/
public interface MessageCommandSucceededEvent<C : MessageCommand<*, *>> :
	CommandSucceededEvent<C, MessageCommandInteractionCreateEvent>

/** Event emitted when an ephemeral message command invocation succeeds. **/
public data class EphemeralMessageCommandSucceededEvent(
	override val command: EphemeralMessageCommand<*>,
	override val event: MessageCommandInteractionCreateEvent,
) : MessageCommandSucceededEvent<EphemeralMessageCommand<*>>

/** Event emitted when a public message command invocation succeeds. **/
public data class PublicMessageCommandSucceededEvent(
	override val command: PublicMessageCommand<*>,
	override val event: MessageCommandInteractionCreateEvent,
) : MessageCommandSucceededEvent<PublicMessageCommand<*>>

// endregion

// region Failed events

/** Basic event emitted when a message command invocation fails. **/
public sealed interface MessageCommandFailedEvent<C : MessageCommand<*, *>> :
	CommandFailedEvent<C, MessageCommandInteractionCreateEvent>

/** Basic event emitted when a message command's checks fail. **/
public interface MessageCommandFailedChecksEvent<C : MessageCommand<*, *>> :
	MessageCommandFailedEvent<C>, CommandFailedChecksEvent<C, MessageCommandInteractionCreateEvent>

/** Event emitted when an ephemeral message command's checks fail. **/
public data class EphemeralMessageCommandFailedChecksEvent(
	override val command: EphemeralMessageCommand<*>,
	override val event: MessageCommandInteractionCreateEvent,
	override val reason: String,
) : MessageCommandFailedChecksEvent<EphemeralMessageCommand<*>>

/** Event emitted when a public message command's checks fail. **/
public data class PublicMessageCommandFailedChecksEvent(
	override val command: PublicMessageCommand<*>,
	override val event: MessageCommandInteractionCreateEvent,
	override val reason: String,
) : MessageCommandFailedChecksEvent<PublicMessageCommand<*>>

/** Basic event emitted when a message command invocation fails with an exception. **/
public interface MessageCommandFailedWithExceptionEvent<C : MessageCommand<*, *>> :
	MessageCommandFailedEvent<C>, CommandFailedWithExceptionEvent<C, MessageCommandInteractionCreateEvent>

/** Event emitted when an ephemeral message command invocation fails with an exception. **/
public data class EphemeralMessageCommandFailedWithExceptionEvent(
	override val command: EphemeralMessageCommand<*>,
	override val event: MessageCommandInteractionCreateEvent,
	override val throwable: Throwable,
) : MessageCommandFailedWithExceptionEvent<EphemeralMessageCommand<*>>

/** Event emitted when a public message command invocation fails with an exception. **/
public data class PublicMessageCommandFailedWithExceptionEvent(
	override val command: PublicMessageCommand<*>,
	override val event: MessageCommandInteractionCreateEvent,
	override val throwable: Throwable,
) : MessageCommandFailedWithExceptionEvent<PublicMessageCommand<*>>

// endregion
