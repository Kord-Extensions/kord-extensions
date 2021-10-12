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
    override: Boolean = false,
    moduleDeclaration: ModuleDeclaration
): Module {
    val moduleObj = module(createdAtStart, override, moduleDeclaration)

    loadKoinModules(moduleObj)

    return moduleObj
}

/** Retrieve the current [Koin] instance. **/
public fun getKoin(): Koin =
    KoinPlatformTools.defaultContext().get()
