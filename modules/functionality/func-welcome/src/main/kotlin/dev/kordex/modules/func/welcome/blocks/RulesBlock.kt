/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.modules.func.welcome.blocks

import dev.kord.common.Color
import dev.kord.rest.builder.message.create.MessageCreateBuilder
import dev.kord.rest.builder.message.embed
import dev.kord.rest.builder.message.modify.MessageModifyBuilder
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Suppress("MagicNumber")
@Serializable
@SerialName("rules")
data class RulesBlock(
	val rules: LinkedHashMap<String, String>,
	val text: String? = null,
	val startingIndex: Int = 1,

	val colors: List<Color> = listOf(
		Color(0xff0000),
		Color(0xff8c00),
		Color(0xe1ff00),
		Color(0x55ff00),
		Color(0x00ff37),
		Color(0x00ffc8),
		Color(0x00aaff),
		Color(0x001eff),
		Color(0x7300ff),
		Color(0xff00ff),
	),
) : Block() {
	init {
		if (rules.isEmpty() || rules.size > 10) {
			error("Must provide up to 10 rules")
		}

		if (colors.size < rules.size) {
			error("${rules.size} rules were provided, but not enough colours (${colors.size})")
		}
	}

	override suspend fun create(builder: MessageCreateBuilder) {
		builder.content = text

		var currentIndex = 0
		var humanIndex = currentIndex + startingIndex

		rules.forEach { (rule, text) ->
			builder.embed {
				title = "$humanIndex. $rule"
				description = text

				color = colors[currentIndex]
			}

			currentIndex += 1
			humanIndex = currentIndex + startingIndex
		}
	}

	override suspend fun edit(builder: MessageModifyBuilder) {
		builder.content = text
		builder.components = mutableListOf()

		var currentIndex = 0
		var humanIndex = currentIndex + startingIndex

		rules.forEach { (rule, text) ->
			builder.embed {
				title = "$humanIndex. $rule"
				description = text

				color = colors[currentIndex]
			}

			currentIndex += 1
			humanIndex = currentIndex + startingIndex
		}
	}
}
