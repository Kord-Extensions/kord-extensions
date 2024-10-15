/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.commands.events

import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kordex.core.ArgumentParsingException
import dev.kordex.core.commands.application.slash.EphemeralSlashCommand
import dev.kordex.core.commands.application.slash.PublicSlashCommand
import dev.kordex.core.commands.application.slash.SlashCommand
import dev.kordex.core.i18n.types.Key

// region Invocation events

/** Basic event emitted when a slash command is invoked. **/
public interface SlashCommandInvocationEvent<C : SlashCommand<*, *, *>> :
	CommandInvocationEvent<C, ChatInputCommandInteractionCreateEvent>

/** Event emitted when an ephemeral slash command is invoked. **/
public data class EphemeralSlashCommandInvocationEvent(
	override val command: EphemeralSlashCommand<*, *>,
	override val event: ChatInputCommandInteractionCreateEvent,
) : SlashCommandInvocationEvent<EphemeralSlashCommand<*, *>>

/** Event emitted when a public slash command is invoked. **/
public data class PublicSlashCommandInvocationEvent(
	override val command: PublicSlashCommand<*, *>,
	override val event: ChatInputCommandInteractionCreateEvent,
) : SlashCommandInvocationEvent<PublicSlashCommand<*, *>>

// endregion

// region Succeeded events

/** Basic event emitted when a slash command invocation succeeds. **/
public interface SlashCommandSucceededEvent<C : SlashCommand<*, *, *>> :
	CommandSucceededEvent<C, ChatInputCommandInteractionCreateEvent>

/** Event emitted when an ephemeral slash command invocation succeeds. **/
public data class EphemeralSlashCommandSucceededEvent(
	override val command: EphemeralSlashCommand<*, *>,
	override val event: ChatInputCommandInteractionCreateEvent,
) : SlashCommandSucceededEvent<EphemeralSlashCommand<*, *>>

/** Event emitted when a public slash command invocation succeeds. **/
public data class PublicSlashCommandSucceededEvent(
	override val command: PublicSlashCommand<*, *>,
	override val event: ChatInputCommandInteractionCreateEvent,
) : SlashCommandSucceededEvent<PublicSlashCommand<*, *>>

// endregion

// region Failed events

/** Basic event emitted when a slash command invocation fails. **/
public sealed interface SlashCommandFailedEvent<C : SlashCommand<*, *, *>> :
	CommandFailedEvent<C, ChatInputCommandInteractionCreateEvent>

/** Basic event emitted when a slash command's checks fail. **/
public interface SlashCommandFailedChecksEvent<C : SlashCommand<*, *, *>> :
	SlashCommandFailedEvent<C>, CommandFailedChecksEvent<C, ChatInputCommandInteractionCreateEvent>

/** Event emitted when an ephemeral slash command's checks fail. **/
public data class EphemeralSlashCommandFailedChecksEvent(
	override val command: EphemeralSlashCommand<*, *>,
	override val event: ChatInputCommandInteractionCreateEvent,
	override val reason: Key,
) : SlashCommandFailedChecksEvent<EphemeralSlashCommand<*, *>>

/** Event emitted when a public slash command's checks fail. **/
public data class PublicSlashCommandFailedChecksEvent(
	override val command: PublicSlashCommand<*, *>,
	override val event: ChatInputCommandInteractionCreateEvent,
	override val reason: Key,
) : SlashCommandFailedChecksEvent<PublicSlashCommand<*, *>>

/** Basic event emitted when a slash command's argument parsing fails'. **/
public interface SlashCommandFailedParsingEvent<C : SlashCommand<*, *, *>> :
	SlashCommandFailedEvent<C>, CommandFailedParsingEvent<C, ChatInputCommandInteractionCreateEvent>

/** Event emitted when an ephemeral slash command's argument parsing fails'. **/
public data class EphemeralSlashCommandFailedParsingEvent(
	override val command: EphemeralSlashCommand<*, *>,
	override val event: ChatInputCommandInteractionCreateEvent,
	override val exception: ArgumentParsingException,
) : SlashCommandFailedParsingEvent<EphemeralSlashCommand<*, *>>

/** Event emitted when a public slash command's argument parsing fails'. **/
public data class PublicSlashCommandFailedParsingEvent(
	override val command: PublicSlashCommand<*, *>,
	override val event: ChatInputCommandInteractionCreateEvent,
	override val exception: ArgumentParsingException,
) : SlashCommandFailedParsingEvent<PublicSlashCommand<*, *>>

/** Basic event emitted when a slash command invocation fails with an exception. **/
public interface SlashCommandFailedWithExceptionEvent<C : SlashCommand<*, *, *>> :
	SlashCommandFailedEvent<C>, CommandFailedWithExceptionEvent<C, ChatInputCommandInteractionCreateEvent>

/** Event emitted when an ephemeral slash command invocation fails with an exception. **/
public data class EphemeralSlashCommandFailedWithExceptionEvent(
	override val command: EphemeralSlashCommand<*, *>,
	override val event: ChatInputCommandInteractionCreateEvent,
	override val throwable: Throwable,
) : SlashCommandFailedWithExceptionEvent<EphemeralSlashCommand<*, *>>

/** Event emitted when a public slash command invocation fails with an exception. **/
public data class PublicSlashCommandFailedWithExceptionEvent(
	override val command: PublicSlashCommand<*, *>,
	override val event: ChatInputCommandInteractionCreateEvent,
	override val throwable: Throwable,
) : SlashCommandFailedWithExceptionEvent<PublicSlashCommand<*, *>>

// endregion
