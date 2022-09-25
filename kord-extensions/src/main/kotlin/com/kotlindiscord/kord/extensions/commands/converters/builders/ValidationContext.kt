/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.commands.converters.builders

import com.kotlindiscord.kord.extensions.DiscordRelayedException
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import org.koin.core.component.inject
import java.util.*

/**
 * Class representing the context for an argument validator. This allows the storage of validation steps and a message
 * for the user.
 *
 * @property T TypeVar representing the current argument type
 * @property value Value of type [T]
 * @property context Command context that triggered this validation
 */
public class ValidationContext<out T>(public val value: T, public val context: CommandContext) : KordExKoinComponent {
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

    /** Whether this validator has passed. **/
    public var passed: Boolean = true

    /** Mark this validator as having passed successfully. **/
    public fun pass() {
        this.passed = true
    }

    /** Mark this validator as having failed, optionally providing a message for the user. **/
    public fun fail(message: String? = null) {
        this.message = message
        this.passed = false
    }

    /**
     * If [value] is `true`, mark this validator as having failed, optionally providing a message for the user.
     *
     * Returns `true` if the validator was marked as having failed, `false` otherwise.
     */
    public fun failIf(value: Boolean, message: String? = null): Boolean {
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
    public suspend fun failIf(message: String? = null, callback: suspend () -> Boolean): Boolean =
        failIf(callback(), message)

    /**
     * If [value] is `false`, mark this validator as having failed, optionally providing a message for the user.
     *
     * Returns `true` if the validator was marked as having failed, `false` otherwise.
     */
    public fun failIfNot(value: Boolean, message: String? = null): Boolean =
        failIf(!value, message)

    /**
     * If [callback] returns `false`, mark this validator as having failed, optionally providing a message for the user.
     *
     * Returns `true` if the validator was marked as having failed, `false` otherwise.
     */
    public suspend fun failIfNot(message: String? = null, callback: suspend () -> Boolean): Boolean =
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

    /** Quick access to translate strings using this validator context's [locale]. **/
    public suspend fun translate(
        key: String,
        bundle: String? = defaultBundle,
        replacements: Array<Any?> = arrayOf()
    ): String =
        translations.translate(key, context.getLocale(), bundleName = bundle, replacements = replacements)

    /** Quick access to translate strings using this validator context's [locale]. **/
    public suspend fun translate(
        key: String,
        replacements: Array<Any?> = arrayOf()
    ): String =
        translations.translate(key, context.getLocale(), bundleName = defaultBundle, replacements = replacements)

    /** Quick access to translate strings using this validator context's [locale]. **/
    public suspend fun translate(
        key: String,
        replacements: Map<String, Any?>
    ): String =
        translations.translate(key, context.getLocale(), bundleName = defaultBundle, replacements = replacements)

    /** Quick access to translate strings using this validator context's [locale]. **/
    public suspend fun translate(
        key: String,
        bundle: String?,
        replacements: Map<String, Any?>
    ): String =
        translations.translate(key, context.getLocale(), bundleName = bundle, replacements = replacements)

    /**
     * If this validator has failed, throw a [DiscordRelayedException] with the translated message, if any.
     */
    @Throws(DiscordRelayedException::class)
    public suspend fun throwIfFailed() {
        if (passed.not()) {
            if (message != null) {
                throw DiscordRelayedException(
                    getTranslatedMessage()!!
                )
            } else {
                error("Validation failed.")
            }
        }
    }

    /** Get the translated validator failure message, if the validator has failed and a message was set. **/
    public suspend fun getTranslatedMessage(): String? =
        if (passed.not() && message != null) {
            translate(errorResponseKey, defaultBundle, replacements = arrayOf(message))
        } else {
            null
        }
}
