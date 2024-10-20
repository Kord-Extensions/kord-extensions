/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

@file:Suppress("StringLiteralDuplication")

package dev.kordex.core.commands.application.slash

import dev.kordex.core.CommandRegistrationException
import dev.kordex.core.InvalidCommandException
import dev.kordex.core.annotations.ExtensionDSL
import dev.kordex.core.commands.Arguments
import dev.kordex.core.components.forms.ModalForm
import dev.kordex.core.i18n.types.Key

private const val SUBCOMMAND_AND_GROUP_LIMIT: Int = 25

// region: Group creation

/**
 * Create a command group, using the given name.
 *
 * Note that only root/top-level commands can contain command groups. An error will be thrown if you try to use
 * this with a subcommand.
 *
 * @param name Name of the command group on Discord.
 * @param body Lambda used to build the [SlashGroup] object.
 */
@ExtensionDSL
public suspend fun SlashCommand<*, *, *>.group(name: Key, body: suspend SlashGroup.() -> Unit): SlashGroup {
	if (parentCommand != null) {
		error("Command groups may not be nested inside subcommands.")
	}

	if (groups.size >= SUBCOMMAND_AND_GROUP_LIMIT) {
		error("Commands may only contain up to $SUBCOMMAND_AND_GROUP_LIMIT command groups.")
	}

	val localizedGroupName = localize(name, true).default

	if (groups[localizedGroupName] != null) {
		error("A command group with the name '$name' has already been registered.")
	}

	val group = SlashGroup(name, this)

	body(group)
	group.validate()

	groups[localizedGroupName] = group

	return group
}

// endregion

// region: Slash commands (Ephemeral)

/**
 * DSL function for easily registering an ephemeral subcommand, with arguments.
 *
 * Use this in your setup function to register a subcommand that may be executed on Discord.
 *
 * @param arguments Arguments builder (probably a reference to the class constructor).
 * @param body Builder lambda used for setting up the slash command object.
 */
@ExtensionDSL
public suspend fun <T : Arguments> SlashCommand<*, *, *>.ephemeralSubCommand(
	arguments: () -> T,
	body: suspend EphemeralSlashCommand<T, ModalForm>.() -> Unit,
): EphemeralSlashCommand<T, ModalForm> {
	val commandObj = EphemeralSlashCommand<T, ModalForm>(
		extension,
		arguments,
		null,
		this,
		parentGroup
	)

	body(commandObj)

	return ephemeralSubCommand(commandObj)
}

/**
 * DSL function for easily registering an ephemeral subcommand, with a modal form.
 *
 * Use this in your setup function to register a subcommand that may be executed on Discord.
 *
 * @param modal ModalForm builder (probably a reference to the class constructor).
 * @param body Builder lambda used for setting up the slash command object.
 */
@ExtensionDSL
@JvmName("ephemeralSubCommand1")
public suspend fun <M : ModalForm> SlashCommand<*, *, *>.ephemeralSubCommand(
	modal: () -> M,
	body: suspend EphemeralSlashCommand<Arguments, M>.() -> Unit,
): EphemeralSlashCommand<Arguments, M> {
	val commandObj = EphemeralSlashCommand<Arguments, M>(
		extension,
		null,
		modal,
		this,
		null
	)

	body(commandObj)

	return ephemeralSubCommand(commandObj)
}

/**
 * DSL function for easily registering an ephemeral subcommand, with a modal form.
 *
 * Use this in your setup function to register a subcommand that may be executed on Discord.
 *
 * @param arguments Arguments builder (probably a reference to the class constructor).
 * @param modal ModalForm builder (probably a reference to the class constructor).
 * @param body Builder lambda used for setting up the slash command object.
 */
@ExtensionDSL
public suspend fun <A : Arguments, M : ModalForm> SlashCommand<*, *, *>.ephemeralSubCommand(
	arguments: () -> A,
	modal: () -> M,
	body: suspend EphemeralSlashCommand<A, M>.() -> Unit,
): EphemeralSlashCommand<A, M> {
	val commandObj = EphemeralSlashCommand(
		extension,
		arguments,
		modal,
		this,
		null
	)

	body(commandObj)

	return ephemeralSubCommand(commandObj)
}

/**
 * Function for registering a custom ephemeral slash command object, for subcommands.
 *
 * You can use this if you have a custom ephemeral slash command subclass you need to register.
 *
 * @param commandObj EphemeralSlashCommand object to register as a subcommand.
 */
@ExtensionDSL
public fun <T : Arguments, M : ModalForm> SlashCommand<*, *, *>.ephemeralSubCommand(
	commandObj: EphemeralSlashCommand<T, M>,
): EphemeralSlashCommand<T, M> {
	commandObj.guildId = null

	if (subCommands.size >= SUBCOMMAND_AND_GROUP_LIMIT) {
		throw InvalidCommandException(
			commandObj.name,
			"Groups may only contain up to $SUBCOMMAND_AND_GROUP_LIMIT commands."
		)
	}

	try {
		commandObj.validate()
		subCommands.add(commandObj)
	} catch (e: CommandRegistrationException) {
		kxLogger.error(e) { "Failed to register subcommand - $e" }
	} catch (e: InvalidCommandException) {
		kxLogger.error(e) { "Failed to register subcommand - $e" }
	}

	return commandObj
}

/**
 * DSL function for easily registering an ephemeral subcommand, without arguments.
 *
 * Use this in your slash command function to register a subcommand that may be executed on Discord.
 *
 * @param body Builder lambda used for setting up the subcommand object.
 */
@ExtensionDSL
public suspend fun SlashCommand<*, *, *>.ephemeralSubCommand(
	body: suspend EphemeralSlashCommand<Arguments, ModalForm>.() -> Unit,
): EphemeralSlashCommand<Arguments, ModalForm> {
	val commandObj = EphemeralSlashCommand<Arguments, ModalForm>(
		extension,
		null,
		null,
		this,
		parentGroup
	)

	body(commandObj)

	return ephemeralSubCommand(commandObj)
}

// endregion

// region: Slash commands (Public)

/**
 * DSL function for easily registering a public subcommand, with arguments.
 *
 * Use this in your setup function to register a subcommand that may be executed on Discord.
 *
 * @param arguments Arguments builder (probably a reference to the class constructor).
 * @param body Builder lambda used for setting up the slash command object.
 */
@ExtensionDSL
public suspend fun <T : Arguments> SlashCommand<*, *, *>.publicSubCommand(
	arguments: () -> T,
	body: suspend PublicSlashCommand<T, ModalForm>.() -> Unit,
): PublicSlashCommand<T, ModalForm> {
	val commandObj = PublicSlashCommand<T, ModalForm>(
		extension,
		arguments,
		null,
		this,
		parentGroup
	)

	body(commandObj)

	return publicSubCommand(commandObj)
}

/**
 * DSL function for easily registering a public subcommand, with a modal form.
 *
 * Use this in your setup function to register a subcommand that may be executed on Discord.
 *
 * @param modal ModalForm builder (probably a reference to the class constructor).
 * @param body Builder lambda used for setting up the slash command object.
 */
@ExtensionDSL
@JvmName("publicSubCommand1")
public suspend fun <M : ModalForm> SlashCommand<*, *, *>.publicSubCommand(
	modal: () -> M,
	body: suspend PublicSlashCommand<Arguments, M>.() -> Unit,
): PublicSlashCommand<Arguments, M> {
	val commandObj = PublicSlashCommand<Arguments, M>(
		extension,
		null,
		modal,
		this,
		null
	)

	body(commandObj)

	return publicSubCommand(commandObj)
}

/**
 * DSL function for easily registering a public subcommand, with arguments and a modal form.
 *
 * Use this in your setup function to register a subcommand that may be executed on Discord.
 *
 * @param arguments Arguments builder (probably a reference to the class constructor).
 * @param modal ModalForm builder (probably a reference to the class constructor).
 * @param body Builder lambda used for setting up the slash command object.
 */
@ExtensionDSL
public suspend fun <A : Arguments, M : ModalForm> SlashCommand<*, *, *>.publicSubCommand(
	arguments: () -> A,
	modal: () -> M,
	body: suspend PublicSlashCommand<A, M>.() -> Unit,
): PublicSlashCommand<A, M> {
	val commandObj = PublicSlashCommand(
		extension,
		arguments,
		modal,
		this
	)

	body(commandObj)

	return publicSubCommand(commandObj)
}

/**
 * Function for registering a custom public slash command object, for subcommands.
 *
 * You can use this if you have a custom public slash command subclass you need to register.
 *
 * @param commandObj PublicSlashCommand object to register as a subcommand.
 */
@ExtensionDSL
public fun <T : Arguments, M : ModalForm> SlashCommand<*, *, *>.publicSubCommand(
	commandObj: PublicSlashCommand<T, M>,
): PublicSlashCommand<T, M> {
	commandObj.guildId = null

	if (subCommands.size >= SUBCOMMAND_AND_GROUP_LIMIT) {
		throw InvalidCommandException(
			commandObj.name,
			"Groups may only contain up to $SUBCOMMAND_AND_GROUP_LIMIT commands."
		)
	}

	try {
		commandObj.validate()
		subCommands.add(commandObj)
	} catch (e: CommandRegistrationException) {
		kxLogger.error(e) { "Failed to register subcommand - $e" }
	} catch (e: InvalidCommandException) {
		kxLogger.error(e) { "Failed to register subcommand - $e" }
	}

	return commandObj
}

/**
 * DSL function for easily registering a public subcommand, without arguments.
 *
 * Use this in your slash command function to register a subcommand that may be executed on Discord.
 *
 * @param body Builder lambda used for setting up the subcommand object.
 */
@ExtensionDSL
public suspend fun SlashCommand<*, *, *>.publicSubCommand(
	body: suspend PublicSlashCommand<Arguments, ModalForm>.() -> Unit,
): PublicSlashCommand<Arguments, ModalForm> {
	val commandObj = PublicSlashCommand<Arguments, ModalForm>(
		extension,
		null,
		null,
		this,
		parentGroup
	)

	body(commandObj)

	return publicSubCommand(commandObj)
}

// endregion

// region: Slash groups (Ephemeral)

/**
 * DSL function for easily registering an ephemeral subcommand, with arguments.
 *
 * Use this in your setup function to register a subcommand that may be executed on Discord.
 *
 * @param arguments Arguments builder (probably a reference to the class constructor).
 * @param body Builder lambda used for setting up the slash command object.
 */
@ExtensionDSL
public suspend fun <T : Arguments> SlashGroup.ephemeralSubCommand(
	arguments: () -> T,
	body: suspend EphemeralSlashCommand<T, ModalForm>.() -> Unit,
): EphemeralSlashCommand<T, ModalForm> {
	val commandObj = EphemeralSlashCommand<T, ModalForm>(
		parent.extension,
		arguments,
		null,
		parent,
		this
	)

	body(commandObj)

	return ephemeralSubCommand(commandObj)
}

/**
 * DSL function for easily registering an ephemeral subcommand, with arguments.
 *
 * Use this in your setup function to register a subcommand that may be executed on Discord.
 *
 * @param arguments Arguments builder (probably a reference to the class constructor).
 * @param modal ModalForm builder (probably a reference to the class constructor).
 * @param body Builder lambda used for setting up the slash command object.
 */
@ExtensionDSL
public suspend fun <T : Arguments, M : ModalForm> SlashGroup.ephemeralSubCommand(
	arguments: () -> T,
	modal: () -> M,
	body: suspend EphemeralSlashCommand<T, M>.() -> Unit,
): EphemeralSlashCommand<T, M> {
	val commandObj = EphemeralSlashCommand<T, M>(
		parent.extension,
		arguments,
		modal,
		parent,
		this
	)

	body(commandObj)

	return ephemeralSubCommand(commandObj)
}

/**
 * DSL function for easily registering an ephemeral subcommand, with a modal.
 *
 * Use this in your setup function to register a subcommand that may be executed on Discord.
 *
 * @param modal ModalForm builder (probably a reference to the class constructor).
 * @param body Builder lambda used for setting up the slash command object.
 */
@ExtensionDSL
@JvmName("ephemeralSubCommand1")
public suspend fun <M : ModalForm> SlashGroup.ephemeralSubCommand(
	modal: () -> M,
	body: suspend EphemeralSlashCommand<Arguments, M>.() -> Unit,
): EphemeralSlashCommand<Arguments, M> {
	val commandObj = EphemeralSlashCommand<Arguments, M>(
		parent.extension,
		null,
		modal,
		parent,
		this
	)

	body(commandObj)

	return ephemeralSubCommand(commandObj)
}

/**
 * Function for registering a custom ephemeral slash command object, for subcommands.
 *
 * You can use this if you have a custom ephemeral slash command subclass you need to register.
 *
 * @param commandObj EphemeralSlashCommand object to register as a subcommand.
 */
@ExtensionDSL
public fun <T : Arguments, M : ModalForm> SlashGroup.ephemeralSubCommand(
	commandObj: EphemeralSlashCommand<T, M>,
): EphemeralSlashCommand<T, M> {
	commandObj.guildId = null

	if (subCommands.size >= SUBCOMMAND_AND_GROUP_LIMIT) {
		throw InvalidCommandException(
			commandObj.name,
			"Groups may only contain up to $SUBCOMMAND_AND_GROUP_LIMIT commands."
		)
	}

	try {
		commandObj.validate()
		subCommands.add(commandObj)
	} catch (e: CommandRegistrationException) {
		logger.error(e) { "Failed to register subcommand - $e" }
	} catch (e: InvalidCommandException) {
		logger.error(e) { "Failed to register subcommand - $e" }
	}

	return commandObj
}

/**
 * DSL function for easily registering an ephemeral subcommand, without arguments.
 *
 * Use this in your slash command function to register a subcommand that may be executed on Discord.
 *
 * @param body Builder lambda used for setting up the subcommand object.
 */
@ExtensionDSL
public suspend fun SlashGroup.ephemeralSubCommand(
	body: suspend EphemeralSlashCommand<Arguments, ModalForm>.() -> Unit,
): EphemeralSlashCommand<Arguments, ModalForm> {
	val commandObj = EphemeralSlashCommand<Arguments, ModalForm>(
		parent.extension,
		null,
		null,
		parent,
		this
	)

	body(commandObj)

	return ephemeralSubCommand(commandObj)
}

// endregion

// region: Slash groups (Public)

/**
 * DSL function for easily registering a public subcommand, with arguments.
 *
 * Use this in your setup function to register a subcommand that may be executed on Discord.
 *
 * @param arguments Arguments builder (probably a reference to the class constructor).
 * @param body Builder lambda used for setting up the slash command object.
 */
@ExtensionDSL
public suspend fun <T : Arguments> SlashGroup.publicSubCommand(
	arguments: () -> T,
	body: suspend PublicSlashCommand<T, ModalForm>.() -> Unit,
): PublicSlashCommand<T, ModalForm> {
	val commandObj = PublicSlashCommand<T, ModalForm>(
		parent.extension,
		arguments,
		null,
		parent,
		this
	)

	body(commandObj)

	return publicSubCommand(commandObj)
}

/**
 * DSL function for easily registering a public subcommand, with a modal.
 *
 * Use this in your setup function to register a subcommand that may be executed on Discord.
 *
 * @param modal ModalForm builder (probably a reference to the class constructor).
 * @param body Builder lambda used for setting up the slash command object.
 */
@ExtensionDSL
@JvmName("publicSubCommand1")
public suspend fun <M : ModalForm> SlashGroup.publicSubCommand(
	modal: () -> M,
	body: suspend PublicSlashCommand<Arguments, M>.() -> Unit,
): PublicSlashCommand<Arguments, M> {
	val commandObj = PublicSlashCommand<Arguments, M>(
		parent.extension,
		null,
		modal,
		parent,
		this
	)

	body(commandObj)

	return publicSubCommand(commandObj)
}

/**
 * DSL function for easily registering a public subcommand, with arguments.
 *
 * Use this in your setup function to register a subcommand that may be executed on Discord.
 *
 * @param arguments Arguments builder (probably a reference to the class constructor).
 * @param modal ModalForm builder (probably a reference to the class constructor).
 * @param body Builder lambda used for setting up the slash command object.
 */
@ExtensionDSL
public suspend fun <T : Arguments, M : ModalForm> SlashGroup.publicSubCommand(
	arguments: () -> T,
	modal: () -> M,
	body: suspend PublicSlashCommand<T, M>.() -> Unit,
): PublicSlashCommand<T, M> {
	val commandObj = PublicSlashCommand<T, M>(
		parent.extension,
		arguments,
		modal,
		parent,
		this
	)

	body(commandObj)

	return publicSubCommand(commandObj)
}

/**
 * Function for registering a custom public slash command object, for subcommands.
 *
 * You can use this if you have a custom public slash command subclass you need to register.
 *
 * @param commandObj PublicSlashCommand object to register as a subcommand.
 */
@ExtensionDSL
public fun <T : Arguments, M : ModalForm> SlashGroup.publicSubCommand(
	commandObj: PublicSlashCommand<T, M>,
): PublicSlashCommand<T, M> {
	commandObj.guildId = null

	if (subCommands.size >= SUBCOMMAND_AND_GROUP_LIMIT) {
		throw InvalidCommandException(
			commandObj.name,
			"Groups may only contain up to $SUBCOMMAND_AND_GROUP_LIMIT commands."
		)
	}

	try {
		commandObj.validate()
		subCommands.add(commandObj)
	} catch (e: CommandRegistrationException) {
		logger.error(e) { "Failed to register subcommand - $e" }
	} catch (e: InvalidCommandException) {
		logger.error(e) { "Failed to register subcommand - $e" }
	}

	return commandObj
}

/**
 * DSL function for easily registering a public subcommand, without arguments.
 *
 * Use this in your slash command function to register a subcommand that may be executed on Discord.
 *
 * @param body Builder lambda used for setting up the subcommand object.
 */
@ExtensionDSL
public suspend fun SlashGroup.publicSubCommand(
	body: suspend PublicSlashCommand<Arguments, ModalForm>.() -> Unit,
): PublicSlashCommand<Arguments, ModalForm> {
	val commandObj = PublicSlashCommand<Arguments, ModalForm>(
		parent.extension,
		null,
		null,
		parent,
		this
	)

	body(commandObj)

	return publicSubCommand(commandObj)
}

// endregion
