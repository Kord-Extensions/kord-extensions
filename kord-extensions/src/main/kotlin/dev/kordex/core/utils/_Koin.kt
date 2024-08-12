/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.utils

import dev.kordex.core.koin.KordExContext
import org.koin.core.Koin
import org.koin.core.module.Module
import org.koin.dsl.ModuleDeclaration
import org.koin.dsl.module

/** Wrapper for [org.koin.dsl.module] that immediately loads the module for the current [Koin] instance. **/
public fun loadModule(
	createdAtStart: Boolean = false,
	moduleDeclaration: ModuleDeclaration,
): Module {
	val moduleObj = module(createdAtStart, moduleDeclaration)

	KordExContext.loadKoinModules(moduleObj)

	return moduleObj
}

/** Retrieve the current [Koin] instance. **/
public fun getKoin(): Koin = KordExContext.get()
