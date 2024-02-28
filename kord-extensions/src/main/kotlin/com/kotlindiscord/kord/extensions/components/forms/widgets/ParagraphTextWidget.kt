/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.components.forms.widgets

import dev.kord.common.entity.TextInputStyle

/** A text widget that supports multiple lines of text. **/
public class ParagraphTextWidget : TextInputWidget<ParagraphTextWidget>() {
	override val style: TextInputStyle = TextInputStyle.Paragraph
}
