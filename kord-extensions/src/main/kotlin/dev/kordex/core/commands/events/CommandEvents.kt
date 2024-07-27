/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.core.commands.events

import dev.kord.core.event.Event
import dev.kordex.core.ArgumentParsingException
import dev.kordex.core.commands.Command
import dev.kordex.core.events.KordExEvent

/**
 * Sealed interface representing a basic command event.
 *
 * @param C Command type
 * @param E Event type
 */
public sealed interface CommandEvent<C : Command, E : Event> : KordExEvent {
	/** Command object that this event concerns. **/
	public val command: C

	/** Event object that triggered this invocation. **/
	public val event: E
}

/** Basic event emitted when a command is invoked. **/
public sealed interface CommandInvocationEvent<C : Command, E : Event> : CommandEvent<C, E>

/** Basic event emitted when a command's invocation succeeds. **/
public sealed interface CommandSucceededEvent<C : Command, E : Event> : CommandEvent<C, E>

/** Basic event emitted when a command's invocation fails, for one reason or another. **/
public sealed interface CommandFailedEvent<C : Command, E : Event> : CommandEvent<C, E>

/** Basic event emitted when a command's checks fail, including for required bot permissions. **/
public sealed interface CommandFailedChecksEvent<C : Command, E : Event> : CommandFailedEvent<C, E> {
	/** Human-readable failure reason. **/
	public val reason: String
}

/** Basic event emitted when a command's argument parsing fails. **/
public sealed interface CommandFailedParsingEvent<C : Command, E : Event> : CommandFailedEvent<C, E> {
	/** Argument parsing exception object. **/
	public val exception: ArgumentParsingException
}

/** Basic event emitted when a command's body invocation fails with an exception. **/
public sealed interface CommandFailedWithExceptionEvent<C : Command, E : Event> : CommandFailedEvent<C, E> {
	/** Exception thrown for this failure. **/
	public val throwable: Throwable
}
