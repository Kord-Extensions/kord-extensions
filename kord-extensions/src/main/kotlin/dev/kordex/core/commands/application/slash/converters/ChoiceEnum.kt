/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.commands.application.slash.converters

import dev.kordex.core.commands.application.slash.converters.impl.EnumChoiceConverter
import dev.kordex.core.i18n.types.Key

/** Interface representing an enum used in the [EnumChoiceConverter]. **/
public interface ChoiceEnum {
	/** Human-readable name to show on Discord. **/
	public val readableName: Key
}
