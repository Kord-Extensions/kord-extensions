/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.components.forms

import com.kotlindiscord.kord.extensions.components.Component
import kotlin.time.Duration

public abstract class Form {
    /** Components that haven't been sorted into rows by [pack] yet. **/
    public open val unsortedComponents: MutableList<Component> = mutableListOf()

    /** Array containing sorted rows of components. **/
    public open val grid: WidgetGrid = arrayOf(
        // Up to 5 rows of components

        mutableListOf(),
        mutableListOf(),
        mutableListOf(),
        mutableListOf(),
        mutableListOf(),
    )

    public abstract val timeout: Duration

    public abstract fun pack()
    public abstract fun update()
    public abstract fun destroy()
}
