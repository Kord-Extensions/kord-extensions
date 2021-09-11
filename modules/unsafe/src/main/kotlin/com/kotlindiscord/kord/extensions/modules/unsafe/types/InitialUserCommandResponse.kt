package com.kotlindiscord.kord.extensions.modules.unsafe.types

import com.kotlindiscord.kord.extensions.commands.application.user.InitialEphemeralUserResponseBuilder
import com.kotlindiscord.kord.extensions.commands.application.user.InitialPublicUserResponseBuilder
import com.kotlindiscord.kord.extensions.modules.unsafe.annotations.UnsafeAPI

/** Sealed class representing the initial response types for an unsafe user command. **/
@UnsafeAPI
public sealed class InitialUserCommandResponse {
    /** Respond with an ephemeral ack, without any content. **/
    public object EphemeralAck : InitialUserCommandResponse()

    /** Respond with a public ack, without any content. **/
    public object PublicAck : InitialUserCommandResponse()

    /** Don't respond. Warning: You may not be able to respond in time! **/
    public object None : InitialUserCommandResponse()

    /**
     * Respond with an ephemeral ack, including message content.
     *
     * @param builder Response builder, containing the message content.
     */
    public data class EphemeralResponse(val builder: InitialEphemeralUserResponseBuilder) :
        InitialUserCommandResponse()

    /**
     * Respond with a public ack, including message content.
     *
     * @param builder Response builder, containing the message content.
     **/
    public data class PublicResponse(val builder: InitialPublicUserResponseBuilder) :
        InitialUserCommandResponse()
}
