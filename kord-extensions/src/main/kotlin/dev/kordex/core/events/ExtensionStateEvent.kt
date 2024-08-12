/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
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
