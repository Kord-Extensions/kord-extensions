/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.checks.types

import dev.kord.core.event.Event
import dev.kordex.core.DiscordRelayedException
import dev.kordex.core.annotations.NotTranslated
import dev.kordex.core.i18n.generated.CoreTranslations
import dev.kordex.core.i18n.types.Key
import dev.kordex.core.koin.KordExKoinComponent
import java.util.*

/**
 * Class representing the context for a check. This allows the storage of check status and a message for the users.
 *
 * @property T TypeVar representing the current event type
 * @property event Event of type [T]
 * @property locale Locale for the current check context
 */
public open class CheckContext<out T : Event>(
	public val event: T,
	public val locale: Locale,
) : KordExKoinComponent {
	/**
	 * Translation key to use for the error response message, if not the default.
	 *
	 * The string pointed to by this variable must accept one replacement value, which is the error message itself.
	 *
	 * **Note:** This *must* be a translation key. A bare string may not work, as the error response function uses
	 * the replacement functionality of the translations system.
	 */
	public var errorResponseKey: Key = CoreTranslations.Checks.responseTemplate

	/** Human-readable message for the user, if any. **/
	public var message: String? = null

	/** Whether this check has passed. **/
	public var passed: Boolean = true

	/** Mark this check as having passed successfully. **/
	public fun pass() {
		this.passed = true
	}

	/** Mark this check as having failed, optionally providing a message for the user. **/
	@NotTranslated
	public fun fail(message: String) {
		this.message = message
		this.passed = false
	}

	/** Mark this check as having failed, optionally providing a message for the user. **/
	public fun fail(message: Key? = null) {
		this.message = message
			?.withLocale(locale)
			?.translate()

		this.passed = false
	}

	/**
	 * If [value] is `true`, mark this check as having failed, optionally providing a message for the user.
	 *
	 * Returns `true` if the check was marked as having failed, `false` otherwise.
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
	 * If [value] is `true`, mark this check as having failed, optionally providing a message for the user.
	 *
	 * Returns `true` if the check was marked as having failed, `false` otherwise.
	 */
	public fun failIf(value: Boolean, message: Key? = null): Boolean {
		if (value) {
			fail(message)

			return true
		}

		return false
	}

	/**
	 * If [callback] returns `true`, mark this check as having failed, optionally providing a message for the user.
	 *
	 * Returns `true` if the check was marked as having failed, `false` otherwise.
	 */
	@NotTranslated
	public suspend fun failIf(message: String, callback: suspend () -> Boolean): Boolean =
		failIf(callback(), message)

	/**
	 * If [callback] returns `true`, mark this check as having failed, optionally providing a message for the user.
	 *
	 * Returns `true` if the check was marked as having failed, `false` otherwise.
	 */
	public suspend fun failIf(message: Key? = null, callback: suspend () -> Boolean): Boolean =
		failIf(callback(), message)

	/**
	 * If [value] is `false`, mark this check as having failed, optionally providing a message for the user.
	 *
	 * Returns `true` if the check was marked as having failed, `false` otherwise.
	 */
	@NotTranslated
	public fun failIfNot(value: Boolean, message: String): Boolean =
		failIf(!value, message)

	/**
	 * If [value] is `false`, mark this check as having failed, optionally providing a message for the user.
	 *
	 * Returns `true` if the check was marked as having failed, `false` otherwise.
	 */
	public fun failIfNot(value: Boolean, message: Key? = null): Boolean =
		failIf(!value, message)

	/**
	 * If [callback] returns `false`, mark this check as having failed, optionally providing a message for the user.
	 *
	 * Returns `true` if the check was marked as having failed, `false` otherwise.
	 */
	@NotTranslated
	public suspend fun failIfNot(message: String, callback: suspend () -> Boolean): Boolean =
		failIfNot(callback(), message)

	/**
	 * If [callback] returns `false`, mark this check as having failed, optionally providing a message for the user.
	 *
	 * Returns `true` if the check was marked as having failed, `false` otherwise.
	 */
	public suspend fun failIfNot(message: Key? = null, callback: suspend () -> Boolean): Boolean =
		failIfNot(callback(), message)

	/**
	 * If [value] is `true`, mark this check as having passed.
	 *
	 * Returns `true` if the check was marked as having passed, `false` otherwise.
	 */
	public fun passIf(value: Boolean): Boolean {
		if (value) {
			pass()

			return true
		}

		return false
	}

	/**
	 * If [callback] returns `true`, mark this check as having passed.
	 *
	 * Returns `true` if the check was marked as having passed, `false` otherwise.
	 */
	public suspend fun passIf(callback: suspend () -> Boolean): Boolean =
		passIf(callback())

	/**
	 * If [value] is `true`, mark this check as having passed.
	 *
	 * Returns `true` if the check was marked as having passed, `false` otherwise.
	 */
	public fun passIfNot(value: Boolean): Boolean =
		passIf(!value)

	/**
	 * If [callback] returns `true`, mark this check as having passed.
	 *
	 * Returns `true` if the check was marked as having passed, `false` otherwise.
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
	 * If this check has failed and a message is set, throw a [DiscordRelayedException] with the translated message.
	 */
	@Throws(DiscordRelayedException::class)
	public fun throwIfFailedWithMessage() {
		if (passed.not() && message != null) {
			throw DiscordRelayedException(
				errorResponseKey
					.withLocale(locale)
					.withOrdinalPlaceholders(message)
			)
		}
	}

	/** Get the translated check failure message, if the check has failed and a message was set. **/
	public fun getTranslatedMessage(): String? =
		getMessageKey()?.translate()

	/**
	 * Get a pre-translation [Key] representing the current check failure message,
	 * if the check has failed, and a message was set.
	 */
	public fun getMessageKey(): Key? =
		if (passed.not() && message != null) {
			errorResponseKey
				.withLocale(locale)
				.withOrdinalPlaceholders(message)
		} else {
			null
		}
}
