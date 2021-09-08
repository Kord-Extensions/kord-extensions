package com.kotlindiscord.kord.extensions.modules.unsafe.types

import com.kotlindiscord.kord.extensions.commands.application.slash.InitialEphemeralSlashResponseBuilder
import com.kotlindiscord.kord.extensions.commands.application.slash.InitialPublicSlashResponseBehavior
import com.kotlindiscord.kord.extensions.modules.unsafe.annotations.UnsafeAPI

@UnsafeAPI
/** Sealed class representing the initial response types for an unsafe slash command. **/
public sealed class InitialSlashCommandResponse {
    /** Respond with an ephemeral ack, without any content. **/
    public object EphemeralAck : InitialSlashCommandResponse()

    /** Respond with a public ack, without any content. **/
    public object PublicAck : InitialSlashCommandResponse()

    /**
     * Respond with an ephemeral ack, including message content.
     *
     * @param builder Response builder, containing the message content.
     */
    public data class EphemeralResponse(val builder: InitialEphemeralSlashResponseBuilder) :
        InitialSlashCommandResponse()

    /**
     * Respond with a public ack, including message content.
     *
     * @param builder Response builder, containing the message content.
     **/
    public data class PublicResponse(val builder: InitialPublicSlashResponseBehavior) :
        InitialSlashCommandResponse()
}
