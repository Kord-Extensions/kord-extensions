@file:OptIn(KordPreview::class)

package com.kotlindiscord.kord.extensions.components.builders

import dev.kord.common.annotation.KordPreview
import dev.kord.rest.builder.component.ActionRowBuilder

/**
 * Button builder representing a link button that directs users to a URL.
 *
 * Either a [label] or [emoji] must be provided. A [url] is also required.
 */
public open class LinkButtonBuilder : ButtonBuilder() {
    /** URL to direct users to when clicked. **/
    public open lateinit var url: String

    public override fun apply(builder: ActionRowBuilder) {
        builder.linkButton(url) {
            emoji = partialEmoji
            label = this@LinkButtonBuilder.label
        }
    }

    public override fun validate() {
        if (label == null && partialEmoji == null) {
            error("Link buttons must have either a label or emoji.")
        }

        if (!this::url.isInitialized) {
            error("Link buttons must have a URL.")
        }
    }
}
