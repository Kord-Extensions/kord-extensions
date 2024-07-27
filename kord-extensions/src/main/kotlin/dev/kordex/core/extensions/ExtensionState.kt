/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.core.extensions

/** Extension states, which describe what state of loading/unloading an extension is currently in. **/
public enum class ExtensionState {
	FAILED_LOADING,
	FAILED_UNLOADING,

	LOADED,
	LOADING,

	UNLOADED,
	UNLOADING,
}
