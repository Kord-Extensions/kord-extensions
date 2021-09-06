package com.kotlindiscord.kord.extensions.components.buttons

import com.kotlindiscord.kord.extensions.components.ComponentWithID
import com.kotlindiscord.kord.extensions.components.types.HasPartialEmoji
import dev.kord.common.entity.DiscordPartialEmoji

public abstract class InteractionButtonWithID : ComponentWithID(), HasPartialEmoji {
    public var label: String? = null
    public override var partialEmoji: DiscordPartialEmoji? = null

    override fun validate() {
        super.validate()

        if (label == null && partialEmoji == null) {
            error("Buttons must have either a label or emoji.")
        }
    }
}
