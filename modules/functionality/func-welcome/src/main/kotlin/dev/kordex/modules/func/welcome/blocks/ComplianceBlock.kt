/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

@file:Suppress("DataClassContainsFunctions")

package dev.kordex.modules.func.welcome.blocks

import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.behavior.getChannelOfOrNull
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.Member
import dev.kord.core.entity.Role
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.event.interaction.ButtonInteractionCreateEvent
import dev.kord.core.event.interaction.InteractionCreateEvent
import dev.kord.rest.builder.message.actionRow
import dev.kord.rest.builder.message.allowedMentions
import dev.kord.rest.builder.message.create.MessageCreateBuilder
import dev.kord.rest.builder.message.modify.MessageModifyBuilder
import dev.kordex.core.DISCORD_BLURPLE
import dev.kordex.core.koin.KordExKoinComponent
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.datetime.Clock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.koin.core.component.inject
import org.koin.java.KoinJavaComponent.inject

@Suppress("MagicNumber")
@Serializable
@SerialName("compliance")
data class ComplianceBlock(
	val id: String,
	val role: Snowflake,
	val title: String,
	val buttonText: String,
	val logChannel: Snowflake,
	val complianceTypeUser: String,

	val template: String = "__**{TITLE}**__\n\n" +

		"By clicking the button below, you certify that:\n\n" +

		"**»** {COMPLIANCE_TYPE_USER}\n\n" +

		"**Please note:** Once you certify the above, you can't revoke your certification. If you decide to " +
		"certify the above when it's not true, you may be punished or removed from the server.",

	val complianceTypeLogs: String = complianceTypeUser
		.replace("You", "They")
		.replace("you", "they"),
) : Block(), InteractionBlock, KordExKoinComponent {
	val kord: Kord by inject()

	private suspend fun getGuildRole(): Role? =
		guild.roles.firstOrNull { it.id == role }

	private fun generateButtonId(): String =
		"compliance/button/${channel.id}/$id"

	private suspend fun handleButton(event: ButtonInteractionCreateEvent) {
		if (event.interaction.componentId != generateButtonId()) {
			return
		}

		val response = event.interaction.deferEphemeralResponse()
		val role = getGuildRole()

		if (role == null) {
			response.respond {
				content = "**Error:** The configured role doesn't seem to exist.\n\n" +

					"Please report this to the server's staff team."
			}

			return
		}

		val member = event.interaction.user.asMember(guild.id)

		if (role.id in member.roleIds) {
			response.respond {
				content = "**Error:** You've already certified this; you can't do so again."
			}
		}

		val logged = logCertification(member)

		if (!logged) {
			response.respond {
				content = "**Error:** The configured compliance logging channel doesn't seem to exist, or it's not " +
					"a text channel.\n\n" +

					"Please report this to the server's staff team."
			}

			return
		}

		member.addRole(role.id)

		response.respond {
			content = "Your certification has been recorded, and the <@&${role.id}> role has been granted. Thanks!"
		}
	}

	private suspend fun logCertification(user: Member): Boolean {
		val channel = guild.getChannelOfOrNull<TextChannel>(logChannel)
			?: return false

		channel.createEmbed {
			title = "Compliance Logging"
			color = DISCORD_BLURPLE
			timestamp = Clock.System.now()

			description = "${user.mention} has certified that:\n\n" +

				"**»** $complianceTypeLogs"

			field {
				inline = true
				name = "Block ID"

				value = id
			}

			field {
				inline = true
				name = "User"

				value = "${user.mention} (`${user.tag}` / `${user.id}`)"
			}
		}

		return true
	}

	private fun getMessageText(): String = template
		.replace("{COMPLIANCE_TYPE_USER}", complianceTypeUser)
		.replace("{ROLE_ID}", role.toString())
		.replace("{ROLE_MENTION}", "<@&$role>")
		.replace("{TITLE}", title)

	override suspend fun create(builder: MessageCreateBuilder) {
		builder.content = getMessageText()
		builder.allowedMentions { }

		builder.actionRow {
			interactionButton(ButtonStyle.Primary, generateButtonId()) {
				label = buttonText
			}
		}
	}

	override suspend fun edit(builder: MessageModifyBuilder) {
		builder.content = getMessageText()
		builder.allowedMentions { }

		builder.actionRow {
			interactionButton(ButtonStyle.Primary, generateButtonId()) {
				label = buttonText
			}
		}
	}

	override suspend fun handleInteraction(event: InteractionCreateEvent) {
		if (event is ButtonInteractionCreateEvent) {
			handleButton(event)
		}
	}
}
