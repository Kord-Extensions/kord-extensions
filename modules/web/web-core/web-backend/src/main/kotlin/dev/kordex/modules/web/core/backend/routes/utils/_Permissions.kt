/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.modules.web.core.backend.routes.utils

import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.entity.Guild
import dev.kord.core.entity.Role
import dev.kordex.core.koin.KordExKoinComponent
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import kotlinx.serialization.Serializable
import org.koin.core.component.inject

@Suppress("FunctionOnlyReturningConstant")
public suspend fun ApplicationCall.allow(): Boolean =
	true

public suspend fun ApplicationCall.deny(body: DenyBuilder.() -> Unit): Boolean {
	response.status(HttpStatusCode.Unauthorized)

	val builder = DenyBuilder()

	body(builder)
	respond(builder)

	return false
}

@Serializable
public class DenyBuilder : KordExKoinComponent {
	private val kord: Kord by inject()

	public var reasonKey: String? = null
	public var reasonPlaceholders: Array<String> = arrayOf()

	public var missingGuilds: MutableList<EntityInfo>? = null
	public var missingPermissions: Permissions? = null
	public var missingRoles: MutableList<EntityInfo>? = null

	public fun reason(key: String, placeholders: Array<Any> = arrayOf()) {
		reasonKey = key

		reasonPlaceholders = placeholders
			.map { it.toString() }
			.toTypedArray()
	}

	public fun missingGuild(guild: Guild) {
		if (missingGuilds == null) {
			missingGuilds = mutableListOf()
		}

		missingGuilds!!.add(EntityInfo(guild.id, guild.name))
	}

	public suspend fun missingGuild(id: Snowflake) {
		if (missingGuilds == null) {
			missingGuilds = mutableListOf()
		}

		val guild = kord.getGuildOrNull(id)

		missingGuilds!!.add(EntityInfo(id, guild?.name))
	}

	public fun missingPermission(perm: Permission) {
		if (missingPermissions == null) {
			missingPermissions = Permissions()
		}

		missingPermissions = missingPermissions!! + perm
	}

	public suspend fun missingPermissions(perms: Permissions) {
		if (missingPermissions == null) {
			missingPermissions = Permissions()
		}

		missingPermissions = missingPermissions!! + perms
	}

	public fun missingRole(role: Role) {
		if (missingRoles == null) {
			missingRoles = mutableListOf()
		}

		missingRoles!!.add(EntityInfo(role.id, role.name))
	}

	public suspend fun missingRole(guildId: Snowflake, id: Snowflake) {
		if (missingRoles == null) {
			missingRoles = mutableListOf()
		}

		val guild = kord.getGuildOrNull(guildId)
		val role = guild?.getRoleOrNull(id)

		missingRoles!!.add(EntityInfo(id, role?.name))
	}

	@Serializable
	public data class EntityInfo(val id: Snowflake, val name: String?)
}
