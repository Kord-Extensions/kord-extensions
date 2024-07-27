/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
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
