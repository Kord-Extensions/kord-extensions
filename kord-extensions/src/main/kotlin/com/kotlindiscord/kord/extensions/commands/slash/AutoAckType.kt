package com.kotlindiscord.kord.extensions.commands.slash

/** Acknowledgement type for autoAck. **/
public sealed class AutoAckType {
    /** Ephemeral acknowledgement. **/
    public object EPHEMERAL : AutoAckType()

    /** Public acknowledgement. **/
    public object PUBLIC : AutoAckType()

    /** Do not ack automatically. **/
    public object NONE : AutoAckType()
}
