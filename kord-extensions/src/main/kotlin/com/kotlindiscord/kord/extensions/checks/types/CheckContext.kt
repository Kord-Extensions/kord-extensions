package com.kotlindiscord.kord.extensions.checks.types

import com.kotlindiscord.kord.extensions.DiscordRelayedException
import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import dev.kord.core.event.Event
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*

/**
 * Class representing the context for a check. This allows the storage of check status and a message for the users.
 *
 * @property T TypeVar representing the current event type
 * @property event Event of type [T]
 * @property locale Locale for the current check context
 */
public class CheckContext<out T : Event>(public val event: T, public val locale: Locale) : KoinComponent {
    /** Translations provider. **/
    public val translations: TranslationsProvider by inject()

    /**
     * Translation key to use for the error response message, if not the default.
     *
     * The string pointed to by this variable must accept one replacement value, which is the error message itself.
     *
     * **Note:** This *must* be a translation key. A bare string may not work, as the error response function uses
     * the replacement functionality of the translations system.
     */
    public var errorResponseKey: String = "checks.responseTemplate"

    /** Translation bundle used by [translate] by default and the error response translation, if not the default. **/
    public var defaultBundle: String? = null

    /** Human-readable message for the user, if any. **/
    public var message: String? = null

    /** Whether this check has passed. **/
    public var passed: Boolean = true

    /** Mark this check as having passed successfully. **/
    public fun pass() {
        this.passed = true
    }

    /** Mark this check as having failed, optionally providing a message for the user. **/
    public fun fail(message: String? = null) {
        this.message = message
        this.passed = false
    }

    /**
     * If [value] is `true`, mark this check as having failed, optionally providing a message for the user.
     *
     * Returns `true` if the check was marked as having failed, `false` otherwise.
     */
    public fun failIf(value: Boolean, message: String? = null): Boolean {
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
    public suspend fun failIf(message: String? = null, callback: suspend () -> Boolean): Boolean =
        failIf(callback(), message)

    /**
     * If [value] is `false`, mark this check as having failed, optionally providing a message for the user.
     *
     * Returns `true` if the check was marked as having failed, `false` otherwise.
     */
    public fun failIfNot(value: Boolean, message: String? = null): Boolean =
        failIf(!value, message)

    /**
     * If [callback] returns `false`, mark this check as having failed, optionally providing a message for the user.
     *
     * Returns `true` if the check was marked as having failed, `false` otherwise.
     */
    public suspend fun failIfNot(message: String? = null, callback: suspend () -> Boolean): Boolean =
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

    /** Quick access to translate strings using this check context's [locale]. **/
    public fun translate(
        key: String,
        bundle: String? = defaultBundle,
        replacements: Array<Any?> = arrayOf()
    ): String =
        translations.translate(key, locale, bundleName = bundle, replacements = replacements)

    /**
     * If this check has failed and a message is set, throw a [DiscordRelayedException] with the translated message.
     */
    @Throws(DiscordRelayedException::class)
    public fun throwIfFailedWithMessage() {
        if (passed.not() && message != null) {
            throw DiscordRelayedException(
                getTranslatedMessage()!!
            )
        }
    }

    /** Get the translated check failure message, if the check has failed and a message was set. **/
    public fun getTranslatedMessage(): String? =
        if (passed.not() && message != null) {
            translate(errorResponseKey, defaultBundle, replacements = arrayOf(message))
        } else {
            null
        }
}
