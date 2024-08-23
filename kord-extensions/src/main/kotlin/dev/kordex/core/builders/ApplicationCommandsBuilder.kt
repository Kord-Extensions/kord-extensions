/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.builders

import dev.kord.common.entity.Snowflake
import dev.kordex.core.annotations.BotBuilderDSL
import dev.kordex.core.checks.types.MessageCommandCheck
import dev.kordex.core.checks.types.SlashCommandCheck
import dev.kordex.core.checks.types.UserCommandCheck
import dev.kordex.core.commands.application.ApplicationCommandRegistry
import dev.kordex.core.commands.application.DefaultApplicationCommandRegistry

/** Builder used for configuring the bot's application command options. **/
@BotBuilderDSL
public class ApplicationCommandsBuilder {
	/** Whether to register and process application commands. Defaults to `true`. **/
	public var enabled: Boolean = true

	/** The guild ID to use for all global application commands. Intended for testing. **/
	public var defaultGuild: Snowflake? = null

	/** Whether to attempt to register the bot's application commands. Intended for multi-instance sharded bots. **/
	public var register: Boolean = true

	/** @suppress Builder that shouldn't be set directly by the user. **/
	public var applicationCommandRegistryBuilder: () -> ApplicationCommandRegistry =
		{ DefaultApplicationCommandRegistry() }

	/**
	 * List of message command checks.
	 *
	 * These checks will be checked against all message commands.
	 */
	public val messageCommandChecks: MutableList<MessageCommandCheck> = mutableListOf()

	/**
	 * List of slash command checks.
	 *
	 * These checks will be checked against all slash commands.
	 */
	public val slashCommandChecks: MutableList<SlashCommandCheck> = mutableListOf()

	/**
	 * List of user command checks.
	 *
	 * These checks will be checked against all user commands.
	 */
	public val userCommandChecks: MutableList<UserCommandCheck> = mutableListOf()

	/** Set a guild ID to use for all global application commands. Intended for testing. **/
	public fun defaultGuild(id: Snowflake?) {
		defaultGuild = id
	}

	/** Set a guild ID to use for all global application commands. Intended for testing. **/
	public fun defaultGuild(id: ULong?) {
		defaultGuild = id?.let { Snowflake(it) }
	}

	/** Set a guild ID to use for all global application commands. Intended for testing. **/
	public fun defaultGuild(id: String?) {
		defaultGuild = id?.let { Snowflake(it) }
	}

	/**
	 * Register the builder used to create the [ApplicationCommandRegistry]. You can change this if you need to make
	 * use of a subclass.
	 */
	public fun applicationCommandRegistry(builder: () -> ApplicationCommandRegistry) {
		applicationCommandRegistryBuilder = builder
	}

	/**
	 * Define a check which must pass for a message command to be executed. This check will be applied to all
	 * message commands.
	 *
	 * A message command may have multiple checks - all checks must pass for the command to be executed.
	 * Checks will be run in the order that they're defined.
	 *
	 * This function can be used DSL-style with a given body, or it can be passed one or more
	 * predefined functions. See the samples for more information.
	 *
	 * @param checks Checks to apply to all slash commands.
	 */
	public fun messageCommandCheck(vararg checks: MessageCommandCheck) {
		checks.forEach { messageCommandChecks.add(it) }
	}

	/**
	 * Overloaded message command check function to allow for DSL syntax.
	 *
	 * @param check Check to apply to all slash commands.
	 */
	public fun messageCommandCheck(check: MessageCommandCheck) {
		messageCommandChecks.add(check)
	}

	/**
	 * Define a check which must pass for a slash command to be executed. This check will be applied to all
	 * slash commands.
	 *
	 * A slash command may have multiple checks - all checks must pass for the command to be executed.
	 * Checks will be run in the order that they're defined.
	 *
	 * This function can be used DSL-style with a given body, or it can be passed one or more
	 * predefined functions. See the samples for more information.
	 *
	 * @param checks Checks to apply to all slash commands.
	 */
	public fun slashCommandCheck(vararg checks: SlashCommandCheck) {
		checks.forEach { slashCommandChecks.add(it) }
	}

	/**
	 * Overloaded slash command check function to allow for DSL syntax.
	 *
	 * @param check Check to apply to all slash commands.
	 */
	public fun slashCommandCheck(check: SlashCommandCheck) {
		slashCommandChecks.add(check)
	}

	/**
	 * Define a check which must pass for a user command to be executed. This check will be applied to all
	 * user commands.
	 *
	 * A user command may have multiple checks - all checks must pass for the command to be executed.
	 * Checks will be run in the order that they're defined.
	 *
	 * This function can be used DSL-style with a given body, or it can be passed one or more
	 * predefined functions. See the samples for more information.
	 *
	 * @param checks Checks to apply to all slash commands.
	 */
	public fun userCommandCheck(vararg checks: UserCommandCheck) {
		checks.forEach { userCommandChecks.add(it) }
	}

	/**
	 * Overloaded user command check function to allow for DSL syntax.
	 *
	 * @param check Check to apply to all slash commands.
	 */
	public fun userCommandCheck(check: UserCommandCheck) {
		userCommandChecks.add(check)
	}
}
