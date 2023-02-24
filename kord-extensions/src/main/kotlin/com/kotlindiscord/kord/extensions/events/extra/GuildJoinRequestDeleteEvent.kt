/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.events.extra

import com.kotlindiscord.kord.extensions.events.KordExEvent
import com.kotlindiscord.kord.extensions.events.extra.models.GuildJoinRequestDelete
import com.kotlindiscord.kord.extensions.utils.getKoin
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.entity.Guild
import dev.kord.core.entity.Member
import dev.kord.core.entity.Strategizable
import dev.kord.core.entity.User
import dev.kord.core.supplier.EntitySupplier
import dev.kord.core.supplier.EntitySupplyStrategy

public class GuildJoinRequestDeleteEvent(
    public val data: GuildJoinRequestDelete,

    override val kord: Kord = getKoin().get(),
    override val supplier: EntitySupplier = kord.defaultSupplier,
) : KordExEvent, Strategizable {
    public val guildId: Snowflake get() = data.guildId
    public val userId: Snowflake get() = data.userId
    public val requestId: Snowflake get() = data.id

    public suspend fun getUser(): User = supplier.getUser(userId)
    public suspend fun getUserOrNull(): User? = supplier.getUserOrNull(userId)

    public suspend fun getMember(): Member = supplier.getMember(guildId, userId)
    public suspend fun getMemberOrNull(): Member? = supplier.getMemberOrNull(guildId, userId)

    public suspend fun getGuild(): Guild = supplier.getGuild(guildId)
    public suspend fun getGuildOrNull(): Guild? = supplier.getGuildOrNull(guildId)

    override fun withStrategy(strategy: EntitySupplyStrategy<*>): GuildJoinRequestDeleteEvent =
        GuildJoinRequestDeleteEvent(data, supplier = strategy.supply(kord))
}
