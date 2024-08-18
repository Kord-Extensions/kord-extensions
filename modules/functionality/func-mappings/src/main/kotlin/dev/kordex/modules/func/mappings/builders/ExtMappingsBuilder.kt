/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.modules.func.mappings.builders

import dev.kordex.core.checks.types.SlashCommandCheck
import me.shedaniel.linkie.Namespace

/** Builder used for configuring the mappings extension. **/
class ExtMappingsBuilder {
	/** List of checks to apply against the name of the command. **/
	val commandChecks: MutableList<suspend (String) -> SlashCommandCheck> = mutableListOf()

	/** List of checks to apply against the namespace corresponding with the command. **/
	val namespaceChecks: MutableList<suspend (Namespace) -> SlashCommandCheck> = mutableListOf()

	/** Register a check that applies against the name of a command, and its message creation event. **/
	fun commandCheck(check: suspend (String) -> SlashCommandCheck) {
		commandChecks.add(check)
	}

	/** Register a check that applies against the mappings namespace for a command, and its message creation event. **/
	fun namespaceCheck(check: suspend (Namespace) -> SlashCommandCheck) {
		namespaceChecks.add(check)
	}
}
