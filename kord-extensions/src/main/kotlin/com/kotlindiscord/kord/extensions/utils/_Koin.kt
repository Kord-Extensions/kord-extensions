/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.utils

import org.koin.core.Koin
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.dsl.ModuleDeclaration
import org.koin.dsl.module
import org.koin.mp.KoinPlatformTools

/** Wrapper for [org.koin.dsl.module] that immediately loads the module for the current [Koin] instance. **/
public fun loadModule(
    createdAtStart: Boolean = false,
    moduleDeclaration: ModuleDeclaration
): Module {
    val moduleObj = module(createdAtStart, moduleDeclaration)

    loadKoinModules(moduleObj)

    return moduleObj
}

/** Retrieve the current [Koin] instance. **/
public fun getKoin(): Koin =
    KoinPlatformTools.defaultContext().get()
