/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core

import dev.kordex.core.commands.Argument
import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.chat.ChatCommand
import dev.kordex.core.commands.converters.builders.ConverterBuilder
import dev.kordex.core.events.EventHandler
import dev.kordex.core.extensions.Extension
import dev.kordex.core.i18n.types.Key
import dev.kordex.parser.StringParser
import java.util.*
import kotlin.reflect.KClass

/**
 * A base class for all custom exceptions in our bot framework.
 */
public open class KordExException : Exception()

/**
 * Exception thrown when a converter builder hasn't been set up properly.
 *
 * @property builder Builder that didn't validate
 * @property reason Reason for the validation failure
 **/
public class InvalidArgumentException(
	public val builder: ConverterBuilder<*>,
	public val reason: String,
) : KordExException() {
	override val message: String = toString()

	override fun toString(): String =
		"Invalid argument: $builder ($reason)"
}

/**
 * Thrown when an attempt to load an [Extension] fails.
 *
 * @param clazz The invalid [Extension] class.
 * @param reason Why this [Extension] is considered invalid.
 */
public class InvalidExtensionException(
	public val clazz: KClass<out Extension>,
	public val reason: String?,
) : KordExException() {
	override val message: String = toString()

	override fun toString(): String {
		val formattedReason = if (reason != null) {
			" ($reason)"
		} else {
			""
		}

		return "Invalid extension class: ${clazz.qualifiedName}$formattedReason"
	}
}

/**
 * Thrown when an [EventHandler] could not be validated.
 *
 * @param reason Why this [EventHandler] is considered invalid.
 */
public class InvalidEventHandlerException(public val reason: String) : KordExException() {
	override val message: String = toString()

	override fun toString(): String = "Invalid event handler: $reason"
}

/**
 * Thrown when an attempt to register a [EventHandler] fails.
 *
 * @param reason Why this [EventHandler] could not be registered.
 */
public class EventHandlerRegistrationException(public val reason: String) : KordExException() {
	override val message: String = toString()

	override fun toString(): String = "Failed to register event handler: $reason"
}

/**
 * Thrown when a command could not be validated.
 *
 * @param name The command name
 * @param reason Why this command is considered invalid.
 */
public class InvalidCommandException(public val name: Key?, public val reason: String) : KordExException() {
	override val message: String = toString()

	override fun toString(): String {
		if (name == null) {
			return "Invalid command: $reason"
		}

		return "Invalid command $name: $reason"
	}
}

/**
 * Thrown when an attempt to register a [ChatCommand] fails.
 *
 * @param name The [ChatCommand] name
 * @param reason Why this [ChatCommand] could not be registered.
 */
public class CommandRegistrationException(public val name: Key, public val reason: String) : KordExException() {
	override val message: String = toString()

	override fun toString(): String = "Failed to register command $name: $reason"
}

/**
 * Thrown when something exceptional happens that the actioning user on Discord needs to be aware of.
 *
 * Provided [reason] will be returned to the user verbatim.
 *
 * @param reason Human-readable reason for the failure. May be translated.
 * @param translationKey Translation key used to create the [reason] string, if any.
 */
public open class DiscordRelayedException(
	public open val reason: Key,
) : KordExException() {
	override val message: String by lazy { toString() }

	public constructor(other: DiscordRelayedException) : this(other.reason)

	override fun toString(): String = reason.toString()
}

/**
 * Thrown when something happens during argument parsing.
 *
 * @param reason Human-readable reason for the failure. May be translated.
 * @param translationKey Translation key used to create the [reason] string, if any.
 * @param argument Current Argument object, if any.
 * @param arguments Arguments object for the command.
 * @param parser Tokenizing string parser used for this parse attempt, if this was a chat command.
 */
public open class ArgumentParsingException(
	public override val reason: Key,
	public val argument: Argument<*>?,
	public val arguments: Arguments,
	public val parser: StringParser?,
) : DiscordRelayedException(reason) {
	override val message: String by lazy { toString() }

	public constructor(other: ArgumentParsingException) :
		this(
			other.reason,
			other.argument, other.arguments,
			other.parser
		)

	override fun toString(): String = reason.toString()
}
