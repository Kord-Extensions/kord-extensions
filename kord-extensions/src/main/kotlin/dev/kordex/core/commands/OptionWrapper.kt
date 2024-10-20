/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.commands

import dev.kord.rest.builder.interaction.AttachmentBuilder
import dev.kord.rest.builder.interaction.BooleanBuilder
import dev.kord.rest.builder.interaction.ChannelBuilder
import dev.kord.rest.builder.interaction.IntegerOptionBuilder
import dev.kord.rest.builder.interaction.MentionableBuilder
import dev.kord.rest.builder.interaction.NumberOptionBuilder
import dev.kord.rest.builder.interaction.OptionsBuilder
import dev.kord.rest.builder.interaction.RoleBuilder
import dev.kord.rest.builder.interaction.StringChoiceBuilder
import dev.kord.rest.builder.interaction.UserBuilder
import dev.kordex.core.annotations.InternalAPI
import dev.kordex.core.i18n.types.Key
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
public open class OptionWrapper<T : OptionsBuilder> @InternalAPI constructor(
	public var displayName: Key,
	public var description: Key,

	public val body: suspend T.() -> Unit,
	public val type: KClass<T>,
) {
	public val modifiers: MutableList<suspend T.() -> Unit> = mutableListOf()

	public fun modify(body: suspend T.() -> Unit) {
		modifiers.add(body)
	}

	public suspend fun apply(builder: T): T {
		body(builder)
		modifiers.forEach { it(builder) }

		return builder
	}

	public suspend fun toKord(): OptionsBuilder = when (type) {
		AttachmentBuilder::class -> {
			this as OptionWrapper<AttachmentBuilder>
			apply(AttachmentBuilder(displayName.key, description.key))
		}

		BooleanBuilder::class -> {
			this as OptionWrapper<BooleanBuilder>
			apply(BooleanBuilder(displayName.key, description.key))
		}

		ChannelBuilder::class -> {
			this as OptionWrapper<ChannelBuilder>
			apply(ChannelBuilder(displayName.key, description.key))
		}

		IntegerOptionBuilder::class -> {
			this as OptionWrapper<IntegerOptionBuilder>
			apply(IntegerOptionBuilder(displayName.key, description.key))
		}

		MentionableBuilder::class -> {
			this as OptionWrapper<MentionableBuilder>
			apply(MentionableBuilder(displayName.key, description.key))
		}

		NumberOptionBuilder::class -> {
			this as OptionWrapper<NumberOptionBuilder>
			apply(NumberOptionBuilder(displayName.key, description.key))
		}

		RoleBuilder::class -> {
			this as OptionWrapper<RoleBuilder>
			apply(RoleBuilder(displayName.key, description.key))
		}

		StringChoiceBuilder::class -> {
			this as OptionWrapper<StringChoiceBuilder>
			apply(StringChoiceBuilder(displayName.key, description.key))
		}

		UserBuilder::class -> {
			this as OptionWrapper<UserBuilder>
			apply(UserBuilder(displayName.key, description.key))
		}

		else -> error("Unknown option builder type: ${type.qualifiedName} ($type)")
	}
}

@OptIn(InternalAPI::class)
public inline fun <reified T : OptionsBuilder> wrapOption(
	displayName: Key,
	description: Key,

	noinline body: suspend T.() -> Unit,
): OptionWrapper<T> = OptionWrapper<T>(displayName, description, body, T::class)
