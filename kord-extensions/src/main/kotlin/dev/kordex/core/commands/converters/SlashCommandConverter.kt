/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.commands.converters

import dev.kord.core.entity.interaction.OptionValue
import dev.kord.rest.builder.interaction.OptionsBuilder
import dev.kordex.core.commands.Argument
import dev.kordex.core.commands.CommandContext

/**
 * Interface representing converters that can be made use of in slash commands.
 */
public interface SlashCommandConverter {
	/**
	 * Return a slash command option that corresponds to this converter.
	 *
	 * Only applicable to converter types that make sense for slash commands.
	 *
	 * TODO: Create wrapping option builder types to store Key objects with their contexts
	 */
	public suspend fun toSlashOption(arg: Argument<*>): OptionsBuilder

	/** Use the given [option] taken straight from the slash command invocation to fill the converter. **/
	public suspend fun parseOption(context: CommandContext, option: OptionValue<*>): Boolean
}
