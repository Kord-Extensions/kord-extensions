/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.core.components

import dev.kord.core.Kord
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kordex.core.ExtensibleBot
import dev.kordex.core.builders.ExtensibleBotBuilder
import dev.kordex.core.commands.application.ApplicationCommandRegistry
import dev.kordex.core.i18n.TranslationsProvider
import dev.kordex.core.koin.KordExKoinComponent
import dev.kordex.core.sentry.SentryAdapter
import org.koin.core.component.inject

/** Abstract class representing a basic Discord component. **/
public abstract class Component : KordExKoinComponent {
	/** Component width, how many "slots" in one row it needs to be added to the row. **/
	public open val unitWidth: Int = 1

	/** Translations provider, for retrieving translations. **/
	public val translationsProvider: TranslationsProvider by inject()

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

	/** Translation bundle, to retrieve translations from. **/
	public open var bundle: String? = null

	/** Validation function, called to ensure the component is valid, throws exceptions if not. **/
	public abstract fun validate()

	/** Called to apply the given component to a Kord [ActionRowBuilder]. **/
	public abstract fun apply(builder: ActionRowBuilder)
}
