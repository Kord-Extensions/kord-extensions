/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.commands.converters

import dev.kord.core.entity.interaction.OptionValue
import dev.kordex.core.commands.Argument
import dev.kordex.core.commands.CommandContext
import dev.kordex.core.commands.OptionWrapper

/**
 * Interface representing converters that can be made use of in slash commands.
 */
public interface SlashCommandConverter {

	public suspend fun toSlashOption(arg: Argument<*>): OptionWrapper<*>

	/** Use the given [option] taken straight from the slash command invocation to fill the converter. **/
	public suspend fun parseOption(context: CommandContext, option: OptionValue<*>): Boolean
}
