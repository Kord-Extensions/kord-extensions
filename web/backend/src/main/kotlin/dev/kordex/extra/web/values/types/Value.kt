/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.extra.web.values.types

import dev.kordex.extra.web.types.Identifier
import kotlinx.serialization.KSerializer

public abstract class Value<I : Any?, O : Any?, T : Any> {
	public abstract val identifier: Identifier
	public abstract val serializer: KSerializer<T>

	public open val writable: Boolean = false

	public abstract fun write(value: I)
	public abstract fun read(): O
}
