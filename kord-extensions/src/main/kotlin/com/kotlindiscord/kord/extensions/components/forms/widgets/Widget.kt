/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.components.forms.widgets

import dev.kord.rest.builder.component.ActionRowBuilder
import java.util.*

/** Abstract type representing a grid-based widget. **/
public abstract class Widget <T : Any?> {
    /** How wide this widget is, in grid cells. **/
    public abstract var width: Int
        protected set

    /** How tall this widget is, in grid cells. **/
    public abstract var height: Int
        protected set

    /** The final value stored in this widget, as provided by the user. **/
    public abstract var value: T
        protected set

    override fun toString(): String =
        "${this::class.simpleName}@${hashCode()} ($width x $height)"

    /** Function called to apply this widget to a Discord action row. **/
    public abstract suspend fun apply(builder: ActionRowBuilder, locale: Locale, bundle: String?)

    /** Function called to ensure that this widget was set up correctly. **/
    public abstract fun validate()
}
