package com.kotlindiscord.kord.extensions.koin

import org.koin.core.Koin
import org.koin.core.component.KoinComponent

/**
 * [KoinComponent] that gives access to dependencies from Koin app within [KordExContext].
 */
public interface KordExKoinComponent : KoinComponent {
    /**
     * Get the associated Koin instance.
     *
     * @throws IllegalStateException KoinApplication not yet started.
     */
    override fun getKoin(): Koin = KordExContext.get()
}
