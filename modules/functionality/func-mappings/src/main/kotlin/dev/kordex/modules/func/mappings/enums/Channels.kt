/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.modules.func.mappings.enums

import dev.kordex.core.commands.application.slash.converters.ChoiceEnum

/**
 * Enum representing available Yarn channels.
 *
 * @property readableName String name used for the channel by Linkie
 */
enum class Channels(override val readableName: String) : ChoiceEnum {
	OFFICIAL("official"),
	SNAPSHOT("snapshot")
}
