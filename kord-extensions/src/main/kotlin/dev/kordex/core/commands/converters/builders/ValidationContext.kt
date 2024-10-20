/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.commands.converters.builders

import dev.kordex.core.DiscordRelayedException
import dev.kordex.core.annotations.NotTranslated
import dev.kordex.core.commands.CommandContext
import dev.kordex.core.i18n.generated.CoreTranslations
import dev.kordex.core.i18n.types.Key
import dev.kordex.core.koin.KordExKoinComponent

/**
 * Class representing the context for an argument validator. This allows the storage of validation steps and a message
 * for the user.
 *
 * @property T TypeVar representing the current argument type
 * @property value Value of type [T]
 * @property context Command context that triggered this validation
 */
public class ValidationContext<out T>(public val value: T, public val context: CommandContext) : KordExKoinComponent {
	/**
	 * Translation key to use for the error response message, if not the default.
	 *
	 * The string pointed to by this variable must accept one ordinal placeholder, the error message itself.
	 */
	public var errorResponseKey: Key = CoreTranslations.Checks.responseTemplate

	/** Human-readable message for the user, if any. **/
	public var message: String? = null

	/** Whether this validator has passed. **/
	public var passed: Boolean = true

	/** Mark this validator as having passed successfully. **/
	public fun pass() {
		this.passed = true
	}

	/** Mark this validator as having failed, optionally providing a message for the user. **/
	@NotTranslated
	public fun fail(message: String) {
		this.message = message
		this.passed = false
	}

	/** Mark this validator as having failed, optionally providing a message for the user. **/
	public suspend fun fail(message: Key? = null) {
		this.message = message
			?.withLocale(context.getLocale())
			?.translate()

		this.passed = false
	}

	/**
	 * If [value] is `true`, mark this validator as having failed, optionally providing a message for the user.
	 *
	 * Returns `true` if the validator was marked as having failed, `false` otherwise.
	 */
	@NotTranslated
	public fun failIf(value: Boolean, message: String): Boolean {
		if (value) {
			fail(message)

			return true
		}

		return false
	}

	/**
	 * If [value] is `true`, mark this validator as having failed, optionally providing a message for the user.
	 *
	 * Returns `true` if the validator was marked as having failed, `false` otherwise.
	 */
	public suspend fun failIf(value: Boolean, message: Key? = null): Boolean {
		if (value) {
			fail(message)

			return true
		}

		return false
	}

	/**
	 * If [callback] returns `true`, mark this validator as having failed, optionally providing a message for the user.
	 *
	 * Returns `true` if the validator was marked as having failed, `false` otherwise.
	 */
	@NotTranslated
	public suspend fun failIf(message: String, callback: suspend () -> Boolean): Boolean =
		failIf(callback(), message)

	/**
	 * If [callback] returns `true`, mark this validator as having failed, optionally providing a message for the user.
	 *
	 * Returns `true` if the validator was marked as having failed, `false` otherwise.
	 */
	public suspend fun failIf(message: Key? = null, callback: suspend () -> Boolean): Boolean =
		failIf(callback(), message)

	/**
	 * If [value] is `false`, mark this validator as having failed, optionally providing a message for the user.
	 *
	 * Returns `true` if the validator was marked as having failed, `false` otherwise.
	 */
	@NotTranslated
	public fun failIfNot(value: Boolean, message: String): Boolean =
		failIf(!value, message)

	/**
	 * If [value] is `false`, mark this validator as having failed, optionally providing a message for the user.
	 *
	 * Returns `true` if the validator was marked as having failed, `false` otherwise.
	 */
	public suspend fun failIfNot(value: Boolean, message: Key? = null): Boolean =
		failIf(!value, message)

	/**
	 * If [callback] returns `false`, mark this validator as having failed, optionally providing a message for the user.
	 *
	 * Returns `true` if the validator was marked as having failed, `false` otherwise.
	 */
	@NotTranslated
	public suspend fun failIfNot(message: String, callback: suspend () -> Boolean): Boolean =
		failIfNot(callback(), message)

	/**
	 * If [callback] returns `false`, mark this validator as having failed, optionally providing a message for the user.
	 *
	 * Returns `true` if the validator was marked as having failed, `false` otherwise.
	 */
	public suspend fun failIfNot(message: Key? = null, callback: suspend () -> Boolean): Boolean =
		failIfNot(callback(), message)

	/**
	 * If [value] is `true`, mark this validator as having passed.
	 *
	 * Returns `true` if the validator was marked as having passed, `false` otherwise.
	 */
	public fun passIf(value: Boolean): Boolean {
		if (value) {
			pass()

			return true
		}

		return false
	}

	/**
	 * If [callback] returns `true`, mark this validator as having passed.
	 *
	 * Returns `true` if the validator was marked as having passed, `false` otherwise.
	 */
	public suspend fun passIf(callback: suspend () -> Boolean): Boolean =
		passIf(callback())

	/**
	 * If [value] is `true`, mark this validator as having passed.
	 *
	 * Returns `true` if the validator was marked as having passed, `false` otherwise.
	 */
	public fun passIfNot(value: Boolean): Boolean =
		passIf(!value)

	/**
	 * If [callback] returns `true`, mark this validator as having passed.
	 *
	 * Returns `true` if the validator was marked as having passed, `false` otherwise.
	 */
	public suspend fun passIfNot(callback: suspend () -> Boolean): Boolean =
		passIfNot(callback())

	/** Call the given block if the Boolean receiver is `true`. **/
	public inline fun <T : Any> Boolean.whenTrue(body: () -> T?): T? {
		if (this) {
			return body()
		}

		return null
	}

	/** Call the given block if the Boolean receiver is `false`. **/
	public inline fun <T : Any> Boolean.whenFalse(body: () -> T?): T? {
		if (!this) {
			return body()
		}

		return null
	}

	/**
	 * If this validator has failed, throw a [DiscordRelayedException] with the translated message, if any.
	 */
	@Throws(DiscordRelayedException::class)
	public suspend fun throwIfFailed() {
		if (passed.not()) {
			if (message != null) {
				throw DiscordRelayedException(
					getMessageKey()!!
				)
			} else {
				error("Validation failed.")
			}
		}
	}

	/** Get the translated validator failure message, if the validator has failed and a message was set. **/
	public suspend fun getTranslatedMessage(): String? =
		getMessageKey()?.translate()

	/** Get a [Key] representing the validator failure message, if the validator has failed and a message was set. **/
	public suspend fun getMessageKey(): Key? =
		if (passed.not() && message != null) {
			errorResponseKey
				.withLocale(context.getLocale())
				.withOrdinalPlaceholders(message)
		} else {
			null
		}
}
