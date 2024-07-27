/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

@file:OptIn(UnsafeAPI::class)

package dev.kordex.modules.dev.unsafe.commands

import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.event.interaction.MessageCommandInteractionCreateEvent
import dev.kord.core.event.interaction.UserCommandInteractionCreateEvent
import dev.kordex.core.ArgumentParsingException
import dev.kordex.core.commands.events.*
import dev.kordex.modules.dev.unsafe.annotations.UnsafeAPI

// region Message commands

/** Event emitted when an unsafe message command is invoked. **/
public data class UnsafeMessageCommandInvocationEvent(
    override val command: UnsafeMessageCommand<*>,
    override val event: MessageCommandInteractionCreateEvent,
) : MessageCommandInvocationEvent<UnsafeMessageCommand<*>>

/** Event emitted when an unsafe message command invocation succeeds. **/
public data class UnsafeMessageCommandSucceededEvent(
    override val command: UnsafeMessageCommand<*>,
    override val event: MessageCommandInteractionCreateEvent,
) : MessageCommandSucceededEvent<UnsafeMessageCommand<*>>

/** Event emitted when an unsafe message command's checks fail. **/
public data class UnsafeMessageCommandFailedChecksEvent(
    override val command: UnsafeMessageCommand<*>,
    override val event: MessageCommandInteractionCreateEvent,
    override val reason: String,
) : MessageCommandFailedChecksEvent<UnsafeMessageCommand<*>>

/** Event emitted when an unsafe message command invocation fails with an exception. **/
public data class UnsafeMessageCommandFailedWithExceptionEvent(
    override val command: UnsafeMessageCommand<*>,
    override val event: MessageCommandInteractionCreateEvent,
    override val throwable: Throwable,
) : MessageCommandFailedWithExceptionEvent<UnsafeMessageCommand<*>>

// endregion

// region Slash commands

/** Event emitted when an unsafe slash command is invoked. **/
public data class UnsafeSlashCommandInvocationEvent(
    override val command: UnsafeSlashCommand<*, *>,
    override val event: ChatInputCommandInteractionCreateEvent,
) : SlashCommandInvocationEvent<UnsafeSlashCommand<*, *>>

/** Event emitted when an unsafe slash command invocation succeeds. **/
public data class UnsafeSlashCommandSucceededEvent(
    override val command: UnsafeSlashCommand<*, *>,
    override val event: ChatInputCommandInteractionCreateEvent,
) : SlashCommandSucceededEvent<UnsafeSlashCommand<*, *>>

/** Event emitted when an unsafe slash command's checks fail. **/
public data class UnsafeSlashCommandFailedChecksEvent(
    override val command: UnsafeSlashCommand<*, *>,
    override val event: ChatInputCommandInteractionCreateEvent,
    override val reason: String,
) : SlashCommandFailedChecksEvent<UnsafeSlashCommand<*, *>>

/** Event emitted when an unsafe slash command's argument parsing fails. **/
public data class UnsafeSlashCommandFailedParsingEvent(
	override val command: UnsafeSlashCommand<*, *>,
	override val event: ChatInputCommandInteractionCreateEvent,
	override val exception: ArgumentParsingException,
) : SlashCommandFailedParsingEvent<UnsafeSlashCommand<*, *>>

/** Event emitted when an unsafe slash command invocation fails with an exception. **/
public data class UnsafeSlashCommandFailedWithExceptionEvent(
    override val command: UnsafeSlashCommand<*, *>,
    override val event: ChatInputCommandInteractionCreateEvent,
    override val throwable: Throwable,
) : SlashCommandFailedWithExceptionEvent<UnsafeSlashCommand<*, *>>

// endregion

// region User commands

/** Event emitted when an unsafe user command is invoked. **/
public data class UnsafeUserCommandInvocationEvent(
    override val command: UnsafeUserCommand<*>,
    override val event: UserCommandInteractionCreateEvent,
) : UserCommandInvocationEvent<UnsafeUserCommand<*>>

/** Event emitted when an unsafe user command invocation succeeds. **/
public data class UnsafeUserCommandSucceededEvent(
    override val command: UnsafeUserCommand<*>,
    override val event: UserCommandInteractionCreateEvent,
) : UserCommandSucceededEvent<UnsafeUserCommand<*>>

/** Event emitted when an unsafe user command's checks fail. **/
public data class UnsafeUserCommandFailedChecksEvent(
    override val command: UnsafeUserCommand<*>,
    override val event: UserCommandInteractionCreateEvent,
    override val reason: String,
) : UserCommandFailedChecksEvent<UnsafeUserCommand<*>>

/** Event emitted when an unsafe user command invocation fails with an exception. **/
public data class UnsafeUserCommandFailedWithExceptionEvent(
    override val command: UnsafeUserCommand<*>,
    override val event: UserCommandInteractionCreateEvent,
    override val throwable: Throwable,
) : UserCommandFailedWithExceptionEvent<UnsafeUserCommand<*>>

// endregion
