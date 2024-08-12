/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.utils

import dev.kord.common.entity.ChannelType
import dev.kordex.core.commands.CommandContext
import dev.kordex.core.i18n.TranslationsProvider
import java.util.*

/** Given a [ChannelType], return a string representing its translation key. **/
public fun ChannelType.toTranslationKey(): String = when (this) {
	ChannelType.DM -> "channelType.dm"
	ChannelType.GroupDM -> "channelType.groupDm"
	ChannelType.GuildCategory -> "channelType.guildCategory"
	ChannelType.GuildNews -> "channelType.guildNews"
	ChannelType.GuildStageVoice -> "channelType.guildStageVoice"
	ChannelType.GuildText -> "channelType.guildText"
	ChannelType.GuildVoice -> "channelType.guildVoice"
	ChannelType.PublicNewsThread -> "channelType.publicNewsThread"
	ChannelType.PublicGuildThread -> "channelType.publicGuildThread"
	ChannelType.PrivateThread -> "channelType.privateThread"
	ChannelType.GuildDirectory -> "channelType.guildDirectory"
	ChannelType.GuildForum -> "channelType.guildForum"
	ChannelType.GuildMedia -> "channelType.guildMedia"

	is ChannelType.Unknown -> "channelType.unknown"
}

/**
 * Given a [CommandContext], translate the [ChannelType] to a human-readable string based on the context's locale.
 */
public suspend fun ChannelType.translate(context: CommandContext): String =
	context.translate(toTranslationKey())

/**
 * Given a locale, translate the [ChannelType] to a human-readable string.
 */
public fun ChannelType.translate(locale: Locale): String =
	getKoin().get<TranslationsProvider>().translate(toTranslationKey(), locale)
