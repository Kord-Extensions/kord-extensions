/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.components.forms.widgets

import com.kotlindiscord.kord.extensions.components.Component
import dev.kord.rest.builder.component.ActionRowBuilder

public abstract class Widget <T : Any?> {
    public abstract var width: Int
        protected set

    public abstract var height: Int
        protected set

    public abstract var value: T
        protected set

    public open lateinit var component: Component
        protected set

    protected val hasComponent: Boolean get() = this::component.isInitialized

    override fun toString(): String =
        "${this::class.simpleName}@${hashCode()} ($width x $height)"

    public abstract suspend fun apply(builder: ActionRowBuilder)
    public abstract fun validate()
}
