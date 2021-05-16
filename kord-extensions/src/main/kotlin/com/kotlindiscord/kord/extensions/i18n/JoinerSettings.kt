package com.kotlindiscord.kord.extensions.i18n

import java.util.*

/**
 * For translations, used to specify how some terms should be joined together.
 *
 * @param name The setting name, used in translations files
 * @param key The corresponding translation key, if any
 * @param spaceBefore Whether a space normally goes before this joiner type
 * @param spaceAfter Whether a space normally goes after this joiner type
 */
public sealed class JoinerSettings(
    public val name: String,
    public val key: String? = null,
    public val spaceBefore: Boolean = false,
    public val spaceAfter: Boolean = false
) {
    /** Use language-equivalent of "and" to join. **/
    public object AND : JoinerSettings("and", "settings.joiner.and", true, true)

    /** Use language-equivalent of a comma to join. **/
    public object COMMA : JoinerSettings("comma", "settings.joiner.comma", false, true)

    /** Use nothing to join. **/
    public object NONE : JoinerSettings("none")

    /** Use a space to join. **/
    public object SPACE : JoinerSettings("space")

    /** Return the string value for this joiner setting object, as needed. **/
    public fun toString(translations: TranslationsProvider, locale: Locale): String? = when {
        key != null -> translations.translate(key, locale)

        this is NONE -> null
        this is SPACE -> " "

        else -> null
    }

    public companion object {
        /**
         * Given the name of a joiner setting (from eg a properties file), return the setting object, or `null` if it
         * can't be found.
         */
        public fun fromName(name: String): JoinerSettings? = when (name) {
            AND.name -> AND
            COMMA.name -> COMMA
            NONE.name -> NONE
            SPACE.name -> SPACE

            else -> null
        }

        /**
         * Given the translation key for a joiner setting (from eg a properties file), return the setting object or
         * `null` if it can't be found.
         */
        public fun fromKey(key: String): JoinerSettings? = when (key) {
            AND.key -> AND
            COMMA.key -> COMMA
            NONE.key -> NONE
            SPACE.key -> SPACE

            else -> null
        }
    }
}
