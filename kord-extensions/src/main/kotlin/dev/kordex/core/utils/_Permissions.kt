/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.utils

import dev.kord.common.entity.Permission
import dev.kordex.core.commands.CommandContext
import dev.kordex.core.i18n.generated.CoreTranslations
import dev.kordex.core.i18n.types.Key
import java.util.*

@Suppress("DEPRECATION", "DEPRECATION_ERROR")
public fun Permission.toTranslationKey(): Key? = when (this) {
	Permission.CreateEvents -> CoreTranslations.Permission.createEvents
	Permission.CreateGuildExpressions -> CoreTranslations.Permission.createGuildExpressions
	Permission.AddReactions -> CoreTranslations.Permission.addReactions
	Permission.Administrator -> CoreTranslations.Permission.administrator
	Permission.AttachFiles -> CoreTranslations.Permission.attachFiles
	Permission.BanMembers -> CoreTranslations.Permission.banMembers
	Permission.ChangeNickname -> CoreTranslations.Permission.changeNickname
	Permission.Connect -> CoreTranslations.Permission.connect
	Permission.CreateInstantInvite -> CoreTranslations.Permission.createInstantInvite
	Permission.CreatePrivateThreads -> CoreTranslations.Permission.createPrivateThreads
	Permission.CreatePublicThreads -> CoreTranslations.Permission.createPublicThreads
	Permission.DeafenMembers -> CoreTranslations.Permission.deafenMembers
	Permission.EmbedLinks -> CoreTranslations.Permission.embedLinks
	Permission.KickMembers -> CoreTranslations.Permission.kickMembers
	Permission.ManageChannels -> CoreTranslations.Permission.manageChannels
	Permission.ManageEvents -> CoreTranslations.Permission.manageEvents
	Permission.ManageGuild -> CoreTranslations.Permission.manageGuild
	Permission.ManageGuildExpressions -> CoreTranslations.Permission.manageExpressions
	Permission.ManageMessages -> CoreTranslations.Permission.manageMessages
	Permission.ManageNicknames -> CoreTranslations.Permission.manageNicknames
	Permission.ManageRoles -> CoreTranslations.Permission.manageRoles
	Permission.ManageThreads -> CoreTranslations.Permission.manageThreads
	Permission.ManageWebhooks -> CoreTranslations.Permission.manageWebhooks
	Permission.MentionEveryone -> CoreTranslations.Permission.mentionEveryone
	Permission.ModerateMembers -> CoreTranslations.Permission.timeoutMembers
	Permission.MoveMembers -> CoreTranslations.Permission.moveMembers
	Permission.MuteMembers -> CoreTranslations.Permission.muteMembers
	Permission.PrioritySpeaker -> CoreTranslations.Permission.prioritySpeaker
	Permission.ReadMessageHistory -> CoreTranslations.Permission.readMessageHistory
	Permission.RequestToSpeak -> CoreTranslations.Permission.requestToSpeak
	Permission.SendMessages -> CoreTranslations.Permission.sendMessages
	Permission.SendMessagesInThreads -> CoreTranslations.Permission.sendMessagesInThreads
	Permission.SendTTSMessages -> CoreTranslations.Permission.sendTTSMessages
	Permission.SendVoiceMessages -> CoreTranslations.Permission.sendVoiceMessages
	Permission.Speak -> CoreTranslations.Permission.speak
	Permission.Stream -> CoreTranslations.Permission.stream
	Permission.UseApplicationCommands -> CoreTranslations.Permission.useApplicationCommands
	Permission.UseEmbeddedActivities -> CoreTranslations.Permission.useEmbeddedActivities
	Permission.UseExternalEmojis -> CoreTranslations.Permission.useExternalEmojis
	Permission.UseExternalSounds -> CoreTranslations.Permission.useExternalSounds
	Permission.UseExternalStickers -> CoreTranslations.Permission.useExternalStickers
	Permission.UseSoundboard -> CoreTranslations.Permission.useSoundboard
	Permission.UseVAD -> CoreTranslations.Permission.useVAD
	Permission.ViewAuditLog -> CoreTranslations.Permission.viewAuditLog
	Permission.ViewChannel -> CoreTranslations.Permission.viewChannel
	Permission.ViewCreatorMonetizationAnalytics -> CoreTranslations.Permission.viewCreatorMonetizationAnalytics
	Permission.ViewGuildInsights -> CoreTranslations.Permission.viewGuildInsights

	is Permission.Unknown -> null
}

/** Because "Stream" is a confusing name, people may look for "Video" instead. **/
public val Permission.Companion.Video: Permission.Stream
	inline get() = Permission.Stream

/** Because it hasn't been called "Moderate Members" since the DMD testing finished. **/
public val Permission.Companion.TimeoutMembers: Permission.ModerateMembers
	inline get() = Permission.ModerateMembers

/** Given a [CommandContext], translate the [Permission] to a human-readable string based on the context's locale. **/
public suspend fun Permission.translate(context: CommandContext): String {
	val key = toTranslationKey()

	return if (key == null) {
		CoreTranslations.Permission.unknown
			.withLocale(context.getLocale())
			.translate(code.value)
	} else {
		key
			.withLocale(context.getLocale())
			.translate()
	}
}

/** Given a locale, translate the [Permission] to a human-readable string. **/
public fun Permission.translate(locale: Locale): String {
	val key = toTranslationKey()

	return if (key == null) {
		CoreTranslations.Permission.unknown
			.withLocale(locale)
			.translate(code.value)
	} else {
		key
			.withLocale(locale)
			.translate()
	}
}
