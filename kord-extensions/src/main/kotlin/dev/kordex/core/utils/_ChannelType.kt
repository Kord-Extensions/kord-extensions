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
import dev.kordex.core.i18n.generated.CoreTranslations
import dev.kordex.core.i18n.types.Key
import java.util.*

/** Given a [ChannelType], return a string representing its translation key. **/
public fun ChannelType.toTranslationKey(): Key = when (this) {
	ChannelType.DM -> CoreTranslations.ChannelType.dm
	ChannelType.GroupDM -> CoreTranslations.ChannelType.groupDm
	ChannelType.GuildCategory -> CoreTranslations.ChannelType.guildCategory
	ChannelType.GuildNews -> CoreTranslations.ChannelType.guildNews
	ChannelType.GuildStageVoice -> CoreTranslations.ChannelType.guildStageVoice
	ChannelType.GuildText -> CoreTranslations.ChannelType.guildText
	ChannelType.GuildVoice -> CoreTranslations.ChannelType.guildVoice
	ChannelType.PublicNewsThread -> CoreTranslations.ChannelType.publicNewsThread
	ChannelType.PublicGuildThread -> CoreTranslations.ChannelType.publicGuildThread
	ChannelType.PrivateThread -> CoreTranslations.ChannelType.privateThread
	ChannelType.GuildDirectory -> CoreTranslations.ChannelType.guildDirectory
	ChannelType.GuildForum -> CoreTranslations.ChannelType.guildForum
	ChannelType.GuildMedia -> CoreTranslations.ChannelType.guildMedia

	is ChannelType.Unknown -> CoreTranslations.ChannelType.unknown
}

/**
 * Given a [CommandContext], translate the [ChannelType] to a human-readable string based on the context's locale.
 */
public suspend fun ChannelType.translate(context: CommandContext): String =
	toTranslationKey()
		.withLocale(context.getLocale())
		.translate()

/**
 * Given a locale, translate the [ChannelType] to a human-readable string.
 */
public fun ChannelType.translate(locale: Locale): String =
	toTranslationKey()
		.withLocale(locale)
		.translate()
