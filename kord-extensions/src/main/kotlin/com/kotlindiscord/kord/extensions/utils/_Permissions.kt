package com.kotlindiscord.kord.extensions.utils

import dev.kord.common.entity.Permission

/** Given a [Permission], return a human-readable English string. **/
public fun Permission.toHumanReadable(): String = when (this) {
    Permission.AddReactions -> "Add Reactions"
    Permission.Administrator -> "Administrator"
    Permission.All -> "All Permissions"
    Permission.AttachFiles -> "Attach Files"
    Permission.BanMembers -> "Ban Members"
    Permission.ChangeNickname -> "Change Nickname"
    Permission.Connect -> "Connect (Voice)"
    Permission.CreateInstantInvite -> "Create Invite"
    Permission.DeafenMembers -> "Deafen Members"
    Permission.EmbedLinks -> "Embed Links"
    Permission.KickMembers -> "Kick Members"
    Permission.ManageChannels -> "Manage Channels"
    Permission.ManageEmojis -> "Manage Emojis"
    Permission.ManageGuild -> "Manage Guild"
    Permission.ManageMessages -> "Manage Messages"
    Permission.ManageNicknames -> "Manage Nicknames"
    Permission.ManageRoles -> "Manage Roles"
    Permission.ManageWebhooks -> "Manage Webhooks"
    Permission.MentionEveryone -> "Mention Everyone"
    Permission.MoveMembers -> "Move Members"
    Permission.MuteMembers -> "Mute Members"
    Permission.PrioritySpeaker -> "Priority Speaker"
    Permission.ReadMessageHistory -> "Read Message History"
    Permission.SendMessages -> "Send Messages"
    Permission.SendTTSMessages -> "Send TTS Messages"
    Permission.Speak -> "Speak (Voice)"
    Permission.UseExternalEmojis -> "Use External Emojis"
    Permission.UseSlashCommands -> "Use Slash Commands"
    Permission.UseVAD -> "Use Voice Activity"
    Permission.ViewAuditLog -> "View Audit Log"
    Permission.ViewChannel -> "View Channel"
    Permission.ViewGuildInsights -> "View Guild Insights"
}
