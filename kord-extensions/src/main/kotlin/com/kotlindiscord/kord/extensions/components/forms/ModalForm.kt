/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.components.forms

import com.kotlindiscord.kord.extensions.components.forms.widgets.LineTextWidget
import com.kotlindiscord.kord.extensions.components.forms.widgets.ParagraphTextWidget

public open class ModalForm : Form() {
    public suspend fun lineText(
        coordinate: CoordinatePair? = null,
        builder: suspend LineTextWidget.() -> Unit,
    ): LineTextWidget {
        val widget = LineTextWidget()

        builder(widget)
        widget.validate()

        grid.setAtCoordinateOrFirstRow(coordinate, widget)

        return widget
    }

    public suspend fun paragraphText(
        coordinate: CoordinatePair? = null,
        builder: suspend ParagraphTextWidget.() -> Unit,
    ): ParagraphTextWidget {
        val widget = ParagraphTextWidget()

        builder(widget)
        widget.validate()

        grid.setAtCoordinateOrFirstRow(coordinate, widget)

        return widget
    }
}
