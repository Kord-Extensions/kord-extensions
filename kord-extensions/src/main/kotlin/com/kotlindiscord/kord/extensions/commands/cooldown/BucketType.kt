package com.kotlindiscord.kord.extensions.commands.cooldown

import dev.kord.core.behavior.channel.MessageChannelBehavior

public sealed class BucketType {
    public object User: BucketType() {
        public fun genKey(user: dev.kord.core.entity.User): String = "U:${user.id.value}"
    }

    public object Member: BucketType() {
        public fun genKey(member: dev.kord.core.entity.Member): String = "G:${member.guildId.value}|U:${member.id.value}"
    }

    public object Guild: BucketType() {
        public fun genKey(guild: dev.kord.core.entity.Guild): String = "G:${guild.id.value}"
    }

    public object Channel: BucketType() {
        public fun genKey(channel: MessageChannelBehavior): String = "C:${channel.id.value}"
    }
}