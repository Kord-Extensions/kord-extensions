package com.kotlindiscord.kord.extensions.utils

import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import dev.kord.common.entity.Permission
import java.util.*

/** Given a [Permission], return a string representing its translation key. **/
public fun Permission.toTranslationKey(): String = when (this) {
    Permission.AddReactions -> "permission.addReactions"
    Permission.Administrator -> "permission.administrator"
    Permission.All -> "permission.all"
    Permission.AttachFiles -> "permission.attachFiles"
    Permission.BanMembers -> "permission.banMembers"
    Permission.ChangeNickname -> "permission.changeNickname"
    Permission.Connect -> "permission.connect"
    Permission.CreateInstantInvite -> "permission.createInstantInvite"
    Permission.DeafenMembers -> "permission.deafenMembers"
    Permission.EmbedLinks -> "permission.embedLinks"
    Permission.KickMembers -> "permission.kickMembers"
    Permission.ManageChannels -> "permission.manageChannels"
    Permission.ManageEmojis -> "permission.manageEmojis"
    Permission.ManageGuild -> "permission.manageGuild"
    Permission.ManageMessages -> "permission.manageMessages"
    Permission.ManageNicknames -> "permission.manageNicknames"
    Permission.ManageRoles -> "permission.manageRoles"
    Permission.ManageWebhooks -> "permission.manageWebhooks"
    Permission.MentionEveryone -> "permission.mentionEveryone"
    Permission.MoveMembers -> "permission.moveMembers"
    Permission.MuteMembers -> "permission.muteMembers"
    Permission.PrioritySpeaker -> "permission.prioritySpeaker"
    Permission.ReadMessageHistory -> "permission.readMessageHistory"
    Permission.RequestToSpeak -> "permission.requestToSpeak"
    Permission.SendMessages -> "permission.sendMessages"
    Permission.SendTTSMessages -> "permission.sendTTSMessages"
    Permission.Speak -> "permission.speak"
    Permission.Stream -> "permission.stream"
    Permission.UseExternalEmojis -> "permission.useExternalEmojis"
    Permission.UseSlashCommands -> "permission.useSlashCommands"
    Permission.UseVAD -> "permission.useVAD"
    Permission.ViewAuditLog -> "permission.viewAuditLog"
    Permission.ViewChannel -> "permission.viewChannel"
    Permission.ViewGuildInsights -> "permission.viewGuildInsights"
}

/** Because "Stream" is a confusing name, people may look for "Video" instead. **/
public val Permission.Video: Permission.Stream
    inline get() = Permission.Stream

/** Given a [CommandContext], translate the permission to a human-readable string based on the context's locale. **/
public suspend fun Permission.translate(context: CommandContext): String =
    context.translate(toTranslationKey())

/** Given a locale, translate the permission to a human-readable string based on the context's locale. **/
public suspend fun Permission.translate(locale: Locale): String =
    getKoin().get<TranslationsProvider>().translate(toTranslationKey(), locale)
