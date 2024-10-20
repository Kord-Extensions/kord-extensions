/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.i18n

import dev.kordex.core.i18n.types.Bundle
import dev.kordex.core.i18n.types.Key

public val EMPTY_KEY: Key = "".toKey()

/** KordEx translation key. **/
public const val KORDEX_KEY: String = "kordex"

/** Default bundle name suffix. **/
public const val DEFAULT_BUNDLE_SUFFIX: String = "strings"

/** Default KordEx translation bundle. **/
public val KORDEX_BUNDLE: Bundle = Bundle("$KORDEX_KEY.$DEFAULT_BUNDLE_SUFFIX")

/** String used to denote an empty translation value - `∅∅∅` (`\u2205\u2205\u2205`). **/
public const val EMPTY_VALUE_STRING: String = "∅∅∅"
