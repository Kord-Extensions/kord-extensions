/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.modules.extra.welcome.blocks

import dev.kord.core.entity.Guild
import dev.kord.core.entity.channel.GuildMessageChannel
import dev.kord.rest.builder.message.create.MessageCreateBuilder
import dev.kord.rest.builder.message.modify.MessageModifyBuilder
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Suppress("UnnecessaryAbstractClass")
@Serializable
abstract class Block {
	@Transient
	lateinit var channel: GuildMessageChannel

	@Transient
	lateinit var guild: Guild

	abstract suspend fun create(builder: MessageCreateBuilder)
	abstract suspend fun edit(builder: MessageModifyBuilder)
}
