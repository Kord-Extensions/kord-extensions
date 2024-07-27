/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.core.commands.converters.impl

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.User
import dev.kord.core.entity.interaction.OptionValue
import dev.kord.core.entity.interaction.UserOptionValue
import dev.kord.core.event.interaction.AutoCompleteInteractionCreateEvent
import dev.kord.rest.builder.interaction.OptionsBuilder
import dev.kord.rest.builder.interaction.UserBuilder
import dev.kordex.core.DiscordRelayedException
import dev.kordex.core.annotations.converters.Converter
import dev.kordex.core.annotations.converters.ConverterType
import dev.kordex.core.commands.Argument
import dev.kordex.core.commands.CommandContext
import dev.kordex.core.commands.chat.ChatCommandContext
import dev.kordex.core.commands.converters.SingleConverter
import dev.kordex.core.commands.converters.Validator
import dev.kordex.core.i18n.DEFAULT_KORDEX_BUNDLE
import dev.kordex.core.utils.users
import dev.kordex.parser.StringParser
import kotlinx.coroutines.flow.firstOrNull

/**
 * Argument converter for discord [User] arguments.
 *
 * This converter supports specifying members by supplying:
 * * A user or member mention
 * * A user ID
 * * The user's tag (`username#discriminator`)
 * * "me" to refer to the user running the command
 *
 * @param useReply Whether to use the author of the replied-to message (if there is one) instead of trying to parse an
 * argument.
 */
@Converter(
	"user",

	types = [ConverterType.LIST, ConverterType.OPTIONAL, ConverterType.SINGLE],
	builderFields = ["public var useReply: Boolean = true"]
)
public class UserConverter(
	private var useReply: Boolean = true,
	override var validator: Validator<User> = null,
) : SingleConverter<User>() {
	override val signatureTypeString: String = "converters.user.signatureType"
	override val bundle: String = DEFAULT_KORDEX_BUNDLE

	override suspend fun parse(parser: StringParser?, context: CommandContext, named: String?): Boolean {
		if (useReply && context is ChatCommandContext<*>) {
			val messageReference = context.message.asMessage().messageReference

			if (messageReference != null) {
				val user = messageReference.message?.asMessage()?.author?.asUserOrNull()

				if (user != null) {
					parsed = user
					return true
				}
			}
		}

		val arg: String = named ?: parser?.parseNext()?.data ?: return false

		if (arg.equals("me", true)) {
			val user = context.getUser()?.asUserOrNull()

			if (user != null) {
				this.parsed = user

				return true
			}
		}

		if (arg.equals("you", true)) {
			this.parsed = bot.kordRef.getSelf()

			return true
		}

		this.parsed = findUser(arg, context)
			?: throw DiscordRelayedException(
				context.translate("converters.user.error.missing", replacements = arrayOf(arg))
			)

		return true
	}

	private suspend fun findUser(arg: String, context: CommandContext): User? =
		if (arg.startsWith("<@") && arg.endsWith(">")) { // It's a mention
			val id: String = arg.substring(2, arg.length - 1).replace("!", "")

			try {
				kord.getUser(Snowflake(id))
			} catch (e: NumberFormatException) {
				throw DiscordRelayedException(
					context.translate("converters.user.error.invalid", replacements = arrayOf(id))
				)
			}
		} else {
			try { // Try for a user ID first
				kord.getUser(Snowflake(arg))
			} catch (e: NumberFormatException) { // It's not an ID, let's try the tag
				if (!arg.contains("#")) {
					null
				} else {
					kord.users.firstOrNull { user ->
						user.tag.equals(arg, true)
					}
				}
			}
		}

	override suspend fun toSlashOption(arg: Argument<*>): OptionsBuilder =
		UserBuilder(arg.displayName, arg.description).apply { required = true }

	override suspend fun parseOption(context: CommandContext, option: OptionValue<*>): Boolean {
		val optionValue = if (context.eventObj is AutoCompleteInteractionCreateEvent) {
			val id = (option as? UserOptionValue)?.value ?: return false

			kord.getUser(id) ?: return false
		} else {
			(option as? UserOptionValue)?.resolvedObject ?: return false
		}

		this.parsed = optionValue

		return true
	}
}
