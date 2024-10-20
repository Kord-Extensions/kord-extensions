/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.modules.func.mappings.arguments

import dev.kordex.modules.func.mappings.enums.Channels
import me.shedaniel.linkie.Namespace

/** An argument base which provides an argument for a mapping channel. **/
@Suppress("UndocumentedPublicProperty")
abstract class MappingWithChannelArguments(namespace: Namespace) : MappingArguments(namespace) {
	abstract val channel: Channels?
}
