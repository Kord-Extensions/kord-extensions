/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
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
