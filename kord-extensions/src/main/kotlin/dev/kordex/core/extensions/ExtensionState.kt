/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
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
