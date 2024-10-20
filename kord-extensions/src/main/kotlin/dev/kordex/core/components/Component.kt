/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.components

import dev.kord.core.Kord
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kordex.core.ExtensibleBot
import dev.kordex.core.builders.ExtensibleBotBuilder
import dev.kordex.core.commands.application.ApplicationCommandRegistry
import dev.kordex.core.koin.KordExKoinComponent
import dev.kordex.core.sentry.SentryAdapter
import org.koin.core.component.inject

/** Abstract class representing a basic Discord component. **/
public abstract class Component : KordExKoinComponent {
	/** Component width, how many "slots" in one row it needs to be added to the row. **/
	public open val unitWidth: Int = 1

	/** Quick access to the command registry. **/
	public val registry: ApplicationCommandRegistry by inject()

	/** Bot settings object. **/
	public val settings: ExtensibleBotBuilder by inject()

	/** Bot object. **/
	public val bot: ExtensibleBot by inject()

	/** Kord instance, backing the ExtensibleBot. **/
	public val kord: Kord by inject()

	/** Sentry adapter, for easy access to Sentry functions. **/
	public val sentry: SentryAdapter by inject()

	/** Validation function, called to ensure the component is valid, throws exceptions if not. **/
	public abstract fun validate()

	/** Called to apply the given component to a Kord [ActionRowBuilder]. **/
	public abstract fun apply(builder: ActionRowBuilder)
}
