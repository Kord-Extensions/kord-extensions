@file:JvmMultifileClass
@file:JvmName("KoinKt")

package com.kotlindiscord.kord.extensions.utils

import org.koin.core.Koin
import org.koin.core.module.Module
import org.koin.dsl.ModuleDeclaration

/** Wrapper for [org.koin.dsl.module] that immediately loads the module for the current [Koin] instance. **/
public fun Koin.module(
    createdAtStart: Boolean = false,
    override: Boolean = false,
    moduleDeclaration: ModuleDeclaration
): Module {
    val module = org.koin.dsl.module(createdAtStart, override, moduleDeclaration)

    loadModules(listOf(module))

    return module
}
