package com.kotlindiscord.kord.extensions.components.buttons

import dev.kord.rest.builder.component.ActionRowBuilder

public open class LinkInteractionButton : InteractionButton() {
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
