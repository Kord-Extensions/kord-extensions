/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.utils

import dev.kordex.core.i18n.types.Key
import dev.kordex.core.types.TranslatableContext

public suspend fun Key.withContext(context: TranslatableContext) =
	withLocale(context.getLocale())
