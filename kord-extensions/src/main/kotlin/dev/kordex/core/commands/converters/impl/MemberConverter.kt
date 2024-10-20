/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

@file:Suppress("StringLiteralDuplication")

package dev.kordex.core.commands.converters.impl

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Member
import dev.kord.core.entity.User
import dev.kord.core.entity.interaction.MemberOptionValue
import dev.kord.core.entity.interaction.OptionValue
import dev.kord.core.event.interaction.AutoCompleteInteractionCreateEvent
import dev.kord.rest.builder.interaction.UserBuilder
import dev.kordex.core.DiscordRelayedException
import dev.kordex.core.annotations.converters.Converter
import dev.kordex.core.annotations.converters.ConverterType
import dev.kordex.core.commands.Argument
import dev.kordex.core.commands.CommandContext
import dev.kordex.core.commands.OptionWrapper
import dev.kordex.core.commands.chat.ChatCommandContext
import dev.kordex.core.commands.converters.SingleConverter
import dev.kordex.core.commands.converters.Validator
import dev.kordex.core.commands.wrapOption
import dev.kordex.core.i18n.generated.CoreTranslations
import dev.kordex.core.i18n.types.Key
import dev.kordex.core.i18n.withContext
import dev.kordex.core.utils.users
import dev.kordex.parser.StringParser
import kotlinx.coroutines.flow.firstOrNull

/**
 * Argument converter for discord [Member] arguments.
 *
 * Members represent Discord users that are part of a guild. This converter supports specifying members by supplying:
 * * A user or member mention
 * * A user ID
 * * The user's tag (`username#discriminator`)
 * * "me" to refer to the member running the command
 *
 * @param requiredGuild Lambda returning a specific guild to require the member to be in, if needed.
 * @param useReply Whether to use the author of the replied-to message (if there is one) instead of trying to parse an
 * argument.
 */
@Converter(
	"member",

	types = [ConverterType.LIST, ConverterType.OPTIONAL, ConverterType.SINGLE],
	imports = ["dev.kord.common.entity.Snowflake"],

	builderFields = [
		"public var requiredGuild: (suspend () -> Snowflake)? = null",
		"public var useReply: Boolean = true",
		"public var requireSameGuild: Boolean = true",
	]
)
public class MemberConverter(
	private var requiredGuild: (suspend () -> Snowflake)? = null,
	private var useReply: Boolean = true,
	private var requireSameGuild: Boolean = true,
	override var validator: Validator<Member> = null,
) : SingleConverter<Member>() {
	override val signatureType: Key = CoreTranslations.Converters.Member.signatureType

	override suspend fun parse(parser: StringParser?, context: CommandContext, named: String?): Boolean {
		val guild = context.getGuild()

		if (requireSameGuild && requiredGuild == null && guild != null) {
			requiredGuild = { guild.id }
		}

		if (useReply && context is ChatCommandContext<*>) {
			val messageReference = context.message.asMessage().messageReference

			if (messageReference != null) {
				val member = messageReference.message?.asMessage()?.getAuthorAsMemberOrNull()

				if (member != null) {
					parsed = member
					return true
				}
			}
		}

		val arg: String = named ?: parser?.parseNext()?.data ?: return false

		if (arg.equals("me", true)) {
			val member = context.getMember()?.asMemberOrNull()

			if (member != null) {
				this.parsed = member

				return true
			}
		}

		if (arg.equals("you", true) && guild != null) {
			val member = bot.kordRef.getSelf().asMemberOrNull(guild.id)

			if (member != null) {
				this.parsed = member

				return true
			}
		}

		parsed = findMember(arg, context)
			?: throw DiscordRelayedException(
				CoreTranslations.Converters.Member.Error.missing
					.withContext(context)
					.withOrdinalPlaceholders(arg)
			)

		return true
	}

	private suspend fun findMember(arg: String, context: CommandContext): Member? {
		val user: User? = if (arg.startsWith("<@") && arg.endsWith(">")) { // Mention
			val id: String = arg.substring(2, arg.length - 1).replace("!", "")

			try {
				kord.getUser(Snowflake(id))
			} catch (e: NumberFormatException) {
				throw DiscordRelayedException(
					CoreTranslations.Converters.Member.Error.invalid
						.withContext(context)
						.withOrdinalPlaceholders(id)
				)
			}
		} else {
			try { // Try for a user ID first
				kord.getUser(Snowflake(arg))
			} catch (e: NumberFormatException) { // Not an ID, let's try the tag
				if (!arg.contains("#")) {
					null
				} else {
					kord.users.firstOrNull { user ->
						user.tag.equals(arg, true)
					}
				}
			}
		}

		val currentGuild = context.getGuild()

		if (requiredGuild != null || requireSameGuild) {
			val requiredGuildId: Snowflake = requiredGuild?.invoke()
				?: currentGuild?.id
				?: return null  // May happen if cache-only resolution doesn't return a guild, or we're in a DM.

			if (requiredGuildId != currentGuild?.id) {
				return null
			}

			return user?.asMember(requiredGuildId)
		}

		return user?.asMember(
			currentGuild?.id
				?: return null
		)
	}

	override suspend fun toSlashOption(arg: Argument<*>): OptionWrapper<UserBuilder> =
		wrapOption(arg.displayName, arg.description) {
			required = true
		}

	override suspend fun parseOption(context: CommandContext, option: OptionValue<*>): Boolean {
		val optionValue = if (context.eventObj is AutoCompleteInteractionCreateEvent) {
			val id = (option as? MemberOptionValue)?.value ?: return false
			val guild = context.getGuild() ?: return false

			kord.getUser(id)?.asMemberOrNull(guild.id) ?: return false
		} else {
			(option as? MemberOptionValue)?.resolvedObject ?: return false
		}

		val guild = context.getGuild()

		if (requireSameGuild && requiredGuild == null && guild != null) {
			requiredGuild = { guild.id }
		}

		val requiredGuildId = requiredGuild?.invoke()

		if (requiredGuildId != null && optionValue.guildId != requiredGuildId) {
			throw DiscordRelayedException(
				CoreTranslations.Converters.Member.Error.invalid
					.withContext(context)
					.withOrdinalPlaceholders(optionValue.tag)
			)
		}

		this.parsed = optionValue

		return true
	}
}
