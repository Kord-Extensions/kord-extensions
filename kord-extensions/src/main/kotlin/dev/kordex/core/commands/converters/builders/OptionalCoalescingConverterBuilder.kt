/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.core.commands.converters.builders

/** Converter builder for optional coalescing converters. **/
public abstract class OptionalCoalescingConverterBuilder<T : Any> : CoalescingConverterBuilder<T?>() {
	override var ignoreErrors: Boolean = false
}
