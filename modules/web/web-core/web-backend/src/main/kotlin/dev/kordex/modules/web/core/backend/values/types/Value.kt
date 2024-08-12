/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.modules.web.core.backend.values.types

import dev.kordex.modules.web.core.backend.types.Identifier
import kotlinx.serialization.KSerializer

public abstract class Value<I : Any?, O : Any?, T : Any> {
	public abstract val identifier: Identifier
	public abstract val serializer: KSerializer<T>

	public open val writable: Boolean = false

	public abstract fun write(value: I)
	public abstract fun read(): O
}
