/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

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
