package com.kotlindiscord.kord.extensions.commands.events

import com.kotlindiscord.kord.extensions.commands.application.user.EphemeralUserCommand
import com.kotlindiscord.kord.extensions.commands.application.user.PublicUserCommand
import com.kotlindiscord.kord.extensions.commands.application.user.UserCommand
import dev.kord.core.event.interaction.UserCommandInteractionCreateEvent

// region Invocation events

/** Basic event emitted when a user command is invoked. **/
public interface UserCommandInvocationEvent<C : UserCommand<*>> :
    CommandInvocationEvent<C, UserCommandInteractionCreateEvent>

/** Event emitted when an ephemeral user command is invoked. **/
public data class EphemeralUserCommandInvocationEvent(
    override val command: EphemeralUserCommand,
    override val event: UserCommandInteractionCreateEvent
) : UserCommandInvocationEvent<EphemeralUserCommand>

/** Event emitted when a public user command is invoked. **/
public data class PublicUserCommandInvocationEvent(
    override val command: PublicUserCommand,
    override val event: UserCommandInteractionCreateEvent
) : UserCommandInvocationEvent<PublicUserCommand>

// endregion

// region Succeeded events

/** Basic event emitted when a user command invocation succeeds. **/
public interface UserCommandSucceededEvent<C : UserCommand<*>> :
    CommandSucceededEvent<C, UserCommandInteractionCreateEvent>

/** Event emitted when an ephemeral user command invocation succeeds. **/
public data class EphemeralUserCommandSucceededEvent(
    override val command: EphemeralUserCommand,
    override val event: UserCommandInteractionCreateEvent
) : UserCommandSucceededEvent<EphemeralUserCommand>

/** Event emitted when a public user command invocation succeeds. **/
public data class PublicUserCommandSucceededEvent(
    override val command: PublicUserCommand,
    override val event: UserCommandInteractionCreateEvent
) : UserCommandSucceededEvent<PublicUserCommand>

// endregion

// region Failed events

/** Basic event emitted when a user command invocation fails. **/
public sealed interface UserCommandFailedEvent<C : UserCommand<*>> :
    CommandFailedEvent<C, UserCommandInteractionCreateEvent>

/** Basic event emitted when a user command's checks fail. **/
public interface UserCommandFailedChecksEvent<C : UserCommand<*>> :
    UserCommandFailedEvent<C>, CommandFailedChecksEvent<C, UserCommandInteractionCreateEvent>

/** Event emitted when an ephemeral user command's checks fail. **/
public data class EphemeralUserCommandFailedChecksEvent(
    override val command: EphemeralUserCommand,
    override val event: UserCommandInteractionCreateEvent,
    override val reason: String,
) : UserCommandFailedChecksEvent<EphemeralUserCommand>

/** Event emitted when a public user command's checks fail. **/
public data class PublicUserCommandFailedChecksEvent(
    override val command: PublicUserCommand,
    override val event: UserCommandInteractionCreateEvent,
    override val reason: String,
) : UserCommandFailedChecksEvent<PublicUserCommand>

/** Basic event emitted when a user command invocation fails with an exception. **/
public interface UserCommandFailedWithExceptionEvent<C : UserCommand<*>> :
    UserCommandFailedEvent<C>, CommandFailedWithExceptionEvent<C, UserCommandInteractionCreateEvent>

/** Event emitted when an ephemeral user command invocation fails with an exception. **/
public data class EphemeralUserCommandFailedWithExceptionEvent(
    override val command: EphemeralUserCommand,
    override val event: UserCommandInteractionCreateEvent,
    override val throwable: Throwable
) : UserCommandFailedWithExceptionEvent<EphemeralUserCommand>

/** Event emitted when a public user command invocation fails with an exception. **/
public data class PublicUserCommandFailedWithExceptionEvent(
    override val command: PublicUserCommand,
    override val event: UserCommandInteractionCreateEvent,
    override val throwable: Throwable
) : UserCommandFailedWithExceptionEvent<PublicUserCommand>

// endregion
