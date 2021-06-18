@file:OptIn(KordPreview::class)

package com.kotlindiscord.kord.extensions.utils

import dev.kord.common.annotation.KordPreview
import dev.kord.core.behavior.interaction.ComponentInteractionBehavior
import dev.kord.core.behavior.interaction.EphemeralInteractionResponseBehavior
import dev.kord.core.behavior.interaction.PublicInteractionResponseBehavior

/** Convenience wrapper for sending an ephemeral ack, optionally deferred, with less characters. **/
public suspend fun ComponentInteractionBehavior.ackEphemeral(
    deferred: Boolean = false
): EphemeralInteractionResponseBehavior = if (deferred) {
    acknowledgeEphemeralDeferredMessageUpdate()
} else {
    acknowledgeEphemeral()
}

/** Convenience wrapper for sending a public ack, optionally deferred, with less characters. **/
public suspend fun ComponentInteractionBehavior.ackPublic(
    deferred: Boolean = false
): PublicInteractionResponseBehavior = if (deferred) {
    acknowledgePublicDeferredMessageUpdate()
} else {
    acknowledgePublic()
}
