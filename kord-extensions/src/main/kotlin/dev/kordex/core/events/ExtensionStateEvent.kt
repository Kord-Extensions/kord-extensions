/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.core.events

import dev.kordex.core.extensions.Extension
import dev.kordex.core.extensions.ExtensionState

/**
 * Event fired when an extension's state changes.
 *
 * @property extension Extension that has a state change
 * @property state Extension's new state
 */
public data class ExtensionStateEvent(
	public val extension: Extension,
	public val state: ExtensionState,
) : KordExEvent
