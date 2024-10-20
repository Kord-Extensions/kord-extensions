/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

@file:OptIn(UnsafeAPI::class)

package dev.kordex.modules.dev.unsafe.commands

import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.event.interaction.MessageCommandInteractionCreateEvent
import dev.kord.core.event.interaction.UserCommandInteractionCreateEvent
import dev.kordex.core.ArgumentParsingException
import dev.kordex.core.commands.events.*
import dev.kordex.core.i18n.types.Key
import dev.kordex.modules.dev.unsafe.annotations.UnsafeAPI
import dev.kordex.modules.dev.unsafe.commands.message.UnsafeMessageCommand
import dev.kordex.modules.dev.unsafe.commands.slash.UnsafeSlashCommand
import dev.kordex.modules.dev.unsafe.commands.user.UnsafeUserCommand

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
	override val reason: Key,
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
	override val reason: Key,
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
	override val reason: Key,
) : UserCommandFailedChecksEvent<UnsafeUserCommand<*>>

/** Event emitted when an unsafe user command invocation fails with an exception. **/
public data class UnsafeUserCommandFailedWithExceptionEvent(
	override val command: UnsafeUserCommand<*>,
	override val event: UserCommandInteractionCreateEvent,
	override val throwable: Throwable,
) : UserCommandFailedWithExceptionEvent<UnsafeUserCommand<*>>

// endregion
