/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.components.forms.widgets

import dev.kord.common.entity.TextInputStyle

/** A text widget that supports a single line of text. **/
public class LineTextWidget : TextInputWidget<LineTextWidget>() {
	override val style: TextInputStyle = TextInputStyle.Short
}
