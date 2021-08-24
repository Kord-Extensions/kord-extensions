package com.kotlindiscord.kord.extensions.checks.types

import com.kotlindiscord.kord.extensions.CommandException
import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import dev.kord.core.event.Event
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*
import kotlin.jvm.Throws

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
    public fun translate(key: String, bundle: String? = null, replacements: Array<Any?> = arrayOf()): String =
        translations.translate(key, locale, bundleName = bundle, replacements = replacements)

    /** If this check has failed and a message is set, throw a `CommandException` with the translated message. **/
    @Throws(CommandException::class)
    public fun throwIfFailedWithMessage() {
        if (passed.not() && message != null) {
            throw CommandException(
                translate("checks.responseTemplate", replacements = arrayOf(message))
            )
        }
    }
}
