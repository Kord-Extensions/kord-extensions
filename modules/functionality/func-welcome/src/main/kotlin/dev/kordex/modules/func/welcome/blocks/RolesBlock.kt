/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

@file:Suppress("DataClassContainsFunctions")

package dev.kordex.modules.func.welcome.blocks

import dev.kord.common.Color
import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.edit
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.Role
import dev.kord.core.event.interaction.ButtonInteractionCreateEvent
import dev.kord.core.event.interaction.InteractionCreateEvent
import dev.kord.core.event.interaction.SelectMenuInteractionCreateEvent
import dev.kord.rest.builder.component.option
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.actionRow
import dev.kord.rest.builder.message.create.MessageCreateBuilder
import dev.kord.rest.builder.message.embed
import dev.kord.rest.builder.message.modify.MessageModifyBuilder
import dev.kordex.core.DISCORD_BLURPLE
import dev.kordex.core.koin.KordExKoinComponent
import dev.kordex.core.utils.emoji
import dev.kordex.core.utils.toReaction
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.toList
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.koin.core.component.inject

@Suppress("MagicNumber")
@Serializable
@SerialName("roles")
data class RolesBlock(
	val id: String,
	val roles: Map<Snowflake, RoleItem>,
	val title: String = "Role Assignment",
	val description: String = "Click the button below to assign yourself any of the following roles.",
	val color: Color = DISCORD_BLURPLE,
	val template: String = "**»** {MENTION} {DESCRIPTION}",
) : Block(), InteractionBlock, KordExKoinComponent {
	val kord: Kord by inject()

	init {
		if (roles.isEmpty() || roles.size > 25) {
			error("Must provide up to 25 roles")
		}
	}

	private suspend fun EmbedBuilder.setUp() {
		val guildRoles = getGuildRoles()

		title = this@RolesBlock.title
		color = this@RolesBlock.color

		description = buildString {
			append(this@RolesBlock.description)

			appendLine()
			appendLine()

			guildRoles.forEach { (id, role) ->
				val roleItem = this@RolesBlock.roles[id]!!

				append(
					template
						.replace("{MENTION}", role.mention)
						.replace("{NAME}", role.name)
						.replace("{ID}", role.id.toString())
						.replace("{DESCRIPTION}", roleItem.description)
						.replace("{EMOJI}", roleItem.emoji ?: "❓")
				)

				appendLine()
			}
		}
	}

	private suspend fun getGuildRoles(): Map<Snowflake, Role> {
		// Each role ID in the order they appear in the YAML
		val sortedRoleIds = roles.toList().map { it.first }

		return guild.roles
			.filter { it.id in roles.keys }
			.toList()
			.sortedBy { sortedRoleIds.indexOf(it.id) }
			.associateBy { it.id }
	}

	private fun generateButtonId(): String =
		"roles/button/${channel.id}/$id"

	private fun generateMenuId(): String =
		"roles/menu/${channel.id}/$id"

	private suspend fun handleButton(event: ButtonInteractionCreateEvent) {
		if (event.interaction.componentId != generateButtonId()) {
			return
		}

		val response = event.interaction.deferEphemeralResponse()

		val guildRoles = getGuildRoles()
		val userRoles = event.interaction.user.asMember(guild.id).roleIds

		response.respond {
			content = "Please select your roles using the menu below."

			actionRow {
				stringSelect(generateMenuId()) {
					guildRoles.forEach { (id, role) ->
						val emojiString = roles[id]!!.emoji

						option("@${role.name}", id.toString()) {
							default = id in userRoles

							if (emojiString != null) {
								emoji(emojiString.toReaction())
							}
						}
					}

					allowedValues = 0..guildRoles.size
				}
			}
		}
	}

	private suspend fun handleMenu(event: SelectMenuInteractionCreateEvent) {
		if (event.interaction.componentId != generateMenuId()) {
			return
		}

		val response = event.interaction.deferEphemeralResponse()

		val guildRoles = getGuildRoles()
		val member = event.interaction.user.asMember(guild.id)
		val userRoles = member.roleIds.filter { it in guildRoles.keys }

		val selectedRoles = event.interaction.values
			.map { Snowflake(it) }
			.filter { it in guildRoles.keys }

		val toAdd = selectedRoles.filterNot { it in userRoles }
		val toRemove = userRoles.filterNot { it in selectedRoles }

		if (toAdd.isEmpty() && toRemove.isEmpty()) {
			response.respond {
				content = "It looks like you picked all the same roles, so no changes have been made."
			}

			return
		}

		member.edit {
			roles = member.roleIds.toMutableSet()

			roles!!.addAll(toAdd)
			roles!!.removeAll(toRemove)
		}

		response.respond {
			content = "Your roles have been updated!"
		}
	}

	override suspend fun create(builder: MessageCreateBuilder) {
		builder.embed { setUp() }

		builder.actionRow {
			interactionButton(ButtonStyle.Primary, generateButtonId()) {
				label = "Pick roles"
			}
		}
	}

	override suspend fun edit(builder: MessageModifyBuilder) {
		builder.embed { setUp() }

		builder.actionRow {
			interactionButton(ButtonStyle.Primary, generateButtonId()) {
				label = "Pick roles"
			}
		}
	}

	override suspend fun handleInteraction(event: InteractionCreateEvent) {
		when (event) {
			is ButtonInteractionCreateEvent -> handleButton(event)
			is SelectMenuInteractionCreateEvent -> handleMenu(event)

			else -> return
		}
	}
}

@Serializable
data class RoleItem(
	val description: String,
	val emoji: String? = null,
)
