/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.components.forms.widgets

import dev.kord.rest.builder.component.ActionRowBuilder
import java.util.*

/** Abstract type representing a grid-based widget. **/
public abstract class Widget<T : Any?> {
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
	public abstract suspend fun apply(builder: ActionRowBuilder, locale: Locale)

	/** Function called to ensure that this widget was set up correctly. **/
	public abstract fun validate()
}
