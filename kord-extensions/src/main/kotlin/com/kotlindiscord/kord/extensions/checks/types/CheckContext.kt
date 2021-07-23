package com.kotlindiscord.kord.extensions.checks.types

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

    /** Human-readable message for the user, if any. **/
    public var message: String? = null

    /** Whether this check has passed. **/
    public var passed: Boolean = false

    /** Mark this check as having passed successfully. **/
    public fun pass() {
        this.passed = true
    }

    /** Mark this check as having failed, optionally providing a message for the user. **/
    public fun fail(message: String? = null) {
        this.message = message
        this.passed = false
    }

    /** Quick access to translate strings using this check context's [locale]. **/
    public fun translate(key: String, bundle: String? = null, replacements: Array<Any?> = arrayOf()): String =
        translations.translate(key, locale, bundleName = bundle, replacements = replacements)
}
