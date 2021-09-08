package com.kotlindiscord.kord.extensions.modules.unsafe.types

import com.kotlindiscord.kord.extensions.commands.application.message.InitialEphemeralMessageResponseBuilder
import com.kotlindiscord.kord.extensions.commands.application.message.InitialPublicMessageResponseBuilder
import com.kotlindiscord.kord.extensions.modules.unsafe.annotations.UnsafeAPI

@UnsafeAPI
/** Sealed class representing the initial response types for an unsafe message command. **/
public sealed class InitialMessageCommandResponse {
    /** Respond with an ephemeral ack, without any content. **/
    public object EphemeralAck : InitialMessageCommandResponse()

    /** Respond with a public ack, without any content. **/
    public object PublicAck : InitialMessageCommandResponse()

    /**
     * Respond with an ephemeral ack, including message content.
     *
     * @param builder Response builder, containing the message content.
     */
    public data class EphemeralResponse(val builder: InitialEphemeralMessageResponseBuilder) :
        InitialMessageCommandResponse()

    /**
     * Respond with a public ack, including message content.
     *
     * @param builder Response builder, containing the message content.
     **/
    public data class PublicResponse(val builder: InitialPublicMessageResponseBuilder) :
        InitialMessageCommandResponse()
}
