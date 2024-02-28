/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.events

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.ExtensionState

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
