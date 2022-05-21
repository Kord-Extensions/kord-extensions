/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

@file:Suppress("unused")

package com.kotlindiscord.kord.extensions.utils

import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import dev.kord.common.entity.Permission
import java.util.*

/** Given a [Permission], return a string representing its translation key. **/
public fun Permission.toTranslationKey(): String? = when (this) {
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
    Permission.ManageEmojisAndStickers -> "permission.manageEmojisAndStickers"
    Permission.ManageEvents -> "permission.manageEvents"
    Permission.ManageGuild -> "permission.manageGuild"
    Permission.ManageMessages -> "permission.manageMessages"
    Permission.ManageNicknames -> "permission.manageNicknames"
    Permission.ManageRoles -> "permission.manageRoles"
    Permission.ManageThreads -> "permission.manageThreads"
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
    Permission.ModerateMembers -> "permission.timeoutMembers"
    Permission.UseExternalEmojis -> "permission.useExternalEmojis"
    Permission.UseApplicationCommands -> "permission.useApplicationCommands"
    Permission.UseVAD -> "permission.useVAD"
    Permission.ViewAuditLog -> "permission.viewAuditLog"
    Permission.ViewChannel -> "permission.viewChannel"
    Permission.ViewGuildInsights -> "permission.viewGuildInsights"

    Permission.CreatePublicThreads -> "permission.createPublicThreads"
    Permission.CreatePrivateThreads -> "permission.createPrivateThreads"
    Permission.SendMessagesInThreads -> "permission.sendMessagesInThreads"

    Permission.UseExternalStickers -> "permission.useExternalStickers"
    Permission.UseEmbeddedActivities -> "permission.useEmbeddedActivities"

    is Permission.Unknown -> null
}

/** Because "Stream" is a confusing name, people may look for "Video" instead. **/
public val Permission.Video: Permission.Stream
    inline get() = Permission.Stream

/** Because it hasn't been called "Moderate Members" since the DMD testing finished. **/
public val Permission.TimeoutMembers: Permission.ModerateMembers
    inline get() = Permission.ModerateMembers

/** Given a [CommandContext], translate the [Permission] to a human-readable string based on the context's locale. **/
public suspend fun Permission.translate(context: CommandContext): String {
    val key = toTranslationKey()

    return if (key == null) {
        context.translate("permission.unknown", replacements = arrayOf(code.value))
    } else {
        context.translate(key)
    }
}

/** Given a locale, translate the [Permission] to a human-readable string. **/
public fun Permission.translate(locale: Locale): String {
    val key = toTranslationKey()

    return if (key == null) {
        getKoin().get<TranslationsProvider>().translate(
            "permission.unknown",
            locale,
            replacements = arrayOf(code.value)
        )
    } else {
        getKoin().get<TranslationsProvider>().translate(key, locale)
    }
}
