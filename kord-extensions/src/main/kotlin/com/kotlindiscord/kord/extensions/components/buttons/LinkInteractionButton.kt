/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.components.buttons

import dev.kord.rest.builder.component.ActionRowBuilder

/** Class representing a linked button component, which opens a URL when clicked. **/
public open class LinkInteractionButton : InteractionButton() {
    /** URL to send the user to when clicked. **/
    public open lateinit var url: String

    override fun validate() {
        super.validate()

        if (!this::url.isInitialized) {
            error("Link buttons must have a URL.")
        }
    }

    override fun apply(builder: ActionRowBuilder) {
        builder.linkButton(url) {
            emoji = partialEmoji
            label = this@LinkInteractionButton.label
        }
    }
}
