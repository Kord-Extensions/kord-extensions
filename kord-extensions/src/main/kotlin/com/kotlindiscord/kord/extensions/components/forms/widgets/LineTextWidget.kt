/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.components.forms.widgets

import dev.kord.common.entity.TextInputStyle

public class LineTextWidget : TextInputWidget<LineTextWidget>() {
    public override var callback: (LineTextWidget.(String) -> Unit)? = null

    override val style: TextInputStyle = TextInputStyle.Short
}
