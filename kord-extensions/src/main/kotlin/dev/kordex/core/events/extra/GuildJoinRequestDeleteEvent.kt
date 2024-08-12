/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.events.extra

import dev.kord.common.annotation.KordExperimental
import dev.kord.common.annotation.KordUnsafe
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.GuildBehavior
import dev.kord.core.behavior.MemberBehavior
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.entity.Guild
import dev.kord.core.entity.Member
import dev.kord.core.entity.Strategizable
import dev.kord.core.entity.User
import dev.kord.core.supplier.EntitySupplier
import dev.kord.core.supplier.EntitySupplyStrategy
import dev.kordex.core.events.KordExEvent
import dev.kordex.core.events.extra.models.GuildJoinRequestDelete
import dev.kordex.core.events.interfaces.MemberEvent
import dev.kordex.core.utils.getKoin

@OptIn(KordUnsafe::class, KordExperimental::class)
public class GuildJoinRequestDeleteEvent(
	public val data: GuildJoinRequestDelete,

	override val kord: Kord = getKoin().get(),
	override val supplier: EntitySupplier = kord.defaultSupplier,
) : KordExEvent, Strategizable, MemberEvent {
	public val guildId: Snowflake get() = data.guildId
	public val userId: Snowflake get() = data.userId
	public val requestId: Snowflake get() = data.id

	override val guild: GuildBehavior = kord.unsafe.guild(guildId)
	override val user: UserBehavior = kord.unsafe.user(userId)
	override val member: MemberBehavior = kord.unsafe.member(guildId, userId)

	public override suspend fun getUser(): User = supplier.getUser(userId)
	public override suspend fun getUserOrNull(): User? = supplier.getUserOrNull(userId)

	public override suspend fun getMember(): Member = supplier.getMember(guildId, userId)
	public override suspend fun getMemberOrNull(): Member? = supplier.getMemberOrNull(guildId, userId)

	public override suspend fun getGuild(): Guild = supplier.getGuild(guildId)
	public override suspend fun getGuildOrNull(): Guild? = supplier.getGuildOrNull(guildId)

	override fun withStrategy(strategy: EntitySupplyStrategy<*>): GuildJoinRequestDeleteEvent =
		GuildJoinRequestDeleteEvent(data, supplier = strategy.supply(kord))
}
