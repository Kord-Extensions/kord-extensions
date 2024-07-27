/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.modules.func.mappings.arguments

/**
 * Indicates that a namespace can map field types
 * and method descriptors to intermediary names.
 */
interface IntermediaryMappable {
	/**
	 * Whether the results should map to named instead of intermediary/hashed.
	 */
	val mapDescriptors: Boolean
}
