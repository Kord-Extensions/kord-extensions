/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.core.koin

import org.koin.core.Koin
import org.koin.core.KoinApplication
import org.koin.core.context.KoinContext
import org.koin.core.error.ApplicationAlreadyStartedException
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration

/**
 * The [KoinContext] for bot instances.
 *
 * This contains the [KoinApplication] and its [Koin] instance for dependency injection.
 *
 * To use this context, implement [KordExKoinComponent].
 *
 * @see org.koin.core.context.GlobalContext
 */
public object KordExContext : KoinContext {
	/** The current [Koin] instance. */
	private var koin: Koin? = null

	/** The current [KoinApplication]. */
	private var koinApp: KoinApplication? = null

	/**
	 * Gets the [Koin] instance.
	 *
	 * @throws IllegalStateException [KoinApplication] has not yet been started.
	 */
	override fun get(): Koin = koin ?: error("KordEx KoinApplication has not been started")

	/** Gets the [Koin] instance or null if the [KoinApplication] has not yet been started. */
	override fun getOrNull(): Koin? = koin

	/** Gets the [KoinApplication] or null if the [KoinApplication] has not yet been started. */
	public fun getKoinApplicationOrNull(): KoinApplication? = koinApp

	/**
	 * Registers a [KoinApplication] to as the current one for this context.
	 *
	 * @param koinApplication The application to registers.
	 *
	 * @throws KoinAppAlreadyStartedException The [KoinApplication] has already been instantiated.
	 */
	private fun register(koinApplication: KoinApplication) {
		if (koin != null) {
			throw ApplicationAlreadyStartedException("KordEx Koin Application has already been started")
		}

		koinApp = koinApplication
		koin = koinApplication.koin
	}

	/** Closes and removes the current [Koin] instance. */
	override fun stopKoin(): Unit = synchronized(this) {
		koin?.close()
		koin = null
	}

	/**
	 * Starts using the provided [KoinApplication] as the current one for this context.
	 *
	 * @param koinApplication The application to start with.
	 *
	 * @throws KoinAppAlreadyStartedException The [KoinApplication] has already been instantiated.
	 */
	override fun startKoin(koinApplication: KoinApplication): KoinApplication = synchronized(this) {
		register(koinApplication)
		koinApplication.createEagerInstances()

		return koinApplication
	}

	/**
	 * Starts using the provided [KoinAppDeclaration] to create the [KoinApplication] for this context.
	 *
	 * @param appDeclaration The application declaration to start with.
	 *
	 * @throws KoinAppAlreadyStartedException The [KoinApplication] has already been instantiated.
	 */
	override fun startKoin(appDeclaration: KoinAppDeclaration): KoinApplication = synchronized(this) {
		val koinApplication = KoinApplication.init()

		register(koinApplication)
		appDeclaration(koinApplication)
		koinApplication.createEagerInstances()

		return koinApplication
	}

	/** Mirroring the global Koin instance implementation. **/
	override fun loadKoinModules(module: Module, createEagerInstances: Boolean): Unit = synchronized(this) {
		get().loadModules(listOf(module), createEagerInstances = createEagerInstances)
	}

	/** Mirroring the global Koin instance implementation. **/
	override fun loadKoinModules(modules: List<Module>, createEagerInstances: Boolean): Unit = synchronized(this) {
		get().loadModules(modules, createEagerInstances = createEagerInstances)
	}

	/**
	 * Unloads a module from the [Koin] instance.
	 *
	 * @param module The module to unload.
	 */
	override fun unloadKoinModules(module: Module): Unit = synchronized(this) {
		get().unloadModules(listOf(module))
	}

	/**
	 * Unloads modules from the [Koin] instance.
	 *
	 * @param modules The modules to unload.
	 */
	override fun unloadKoinModules(modules: List<Module>): Unit = synchronized(this) {
		get().unloadModules(modules)
	}
}
