/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.components.forms

import kotlin.time.Duration

/**
 * Abstract class representing a form.
 *
 * It's intended that this be expanded past [ModalForm] and be transformed into something that can be used directly
 * with messages as well - but that's a job to be done by someone that needs it.
 */
@Suppress("UnnecessaryAbstractClass")
public abstract class Form {
	/** How long to wait before we stop waiting for this modal to be submitted. **/
	public open val timeout: Duration = Duration.ZERO

//    /** Widgets that haven't been sorted into the grid by [pack] yet. **/
//    public open val unsortedComponents: MutableList<Component> = mutableListOf()

	/** A grid containing rows of widgets. **/
	public open val grid: WidgetGrid = WidgetGrid()

//    public abstract fun pack()
//    public abstract fun update()
//    public abstract fun destroy()
}
