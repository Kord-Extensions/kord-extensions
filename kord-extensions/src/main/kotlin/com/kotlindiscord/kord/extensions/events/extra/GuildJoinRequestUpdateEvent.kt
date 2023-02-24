/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.events.extra

import com.kotlindiscord.kord.extensions.events.KordExEvent
import com.kotlindiscord.kord.extensions.events.extra.models.ApplicationStatus
import com.kotlindiscord.kord.extensions.events.extra.models.GuildJoinRequest
import com.kotlindiscord.kord.extensions.events.extra.models.GuildJoinRequestUpdate
import com.kotlindiscord.kord.extensions.utils.getKoin
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.entity.Guild
import dev.kord.core.entity.Member
import dev.kord.core.entity.Strategizable
import dev.kord.core.entity.User
import dev.kord.core.supplier.EntitySupplier
import dev.kord.core.supplier.EntitySupplyStrategy

public class GuildJoinRequestUpdateEvent(
    public val data: GuildJoinRequestUpdate,

    override val kord: Kord = getKoin().get(),
    override val supplier: EntitySupplier = kord.defaultSupplier,
) : KordExEvent, Strategizable {
    public val status: ApplicationStatus get() = data.status
    public val guildId: Snowflake get() = data.guildId
    public val userId: Snowflake get() = data.request.userId
    public val requestId: Snowflake get() = data.request.id

    public val request: GuildJoinRequest get() = data.request

    public suspend fun getUser(): User = supplier.getUser(userId)
    public suspend fun getUserOrNull(): User? = supplier.getUserOrNull(userId)

    public suspend fun getMember(): Member = supplier.getMember(guildId, userId)
    public suspend fun getMemberOrNull(): Member? = supplier.getMemberOrNull(guildId, userId)

    public suspend fun getGuild(): Guild = supplier.getGuild(guildId)
    public suspend fun getGuildOrNull(): Guild? = supplier.getGuildOrNull(guildId)

    override fun withStrategy(strategy: EntitySupplyStrategy<*>): GuildJoinRequestUpdateEvent =
        GuildJoinRequestUpdateEvent(data, supplier = strategy.supply(kord))
}
