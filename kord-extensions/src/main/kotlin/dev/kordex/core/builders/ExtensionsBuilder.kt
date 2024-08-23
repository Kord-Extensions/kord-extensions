/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.builders

import dev.kordex.core.annotations.BotBuilderDSL
import dev.kordex.core.builders.extensions.HelpExtensionBuilder
import dev.kordex.core.builders.extensions.SentryExtensionBuilder
import dev.kordex.core.extensions.Extension

/** Builder used for configuring the bot's extension options, and registering custom extensions. **/
@BotBuilderDSL
public open class ExtensionsBuilder {
	/** @suppress Internal list that shouldn't be modified by the user directly. **/
	public open val extensions: MutableList<() -> Extension> = mutableListOf()

	/** @suppress Help extension builder. **/
	public open val helpExtensionBuilder: HelpExtensionBuilder = HelpExtensionBuilder()

	/** @suppress Sentry extension builder. **/
	public open val sentryExtensionBuilder: SentryExtensionBuilder = SentryExtensionBuilder()

	/** Add a custom extension to the bot via a builder - probably the extension constructor. **/
	public open fun add(builder: () -> Extension) {
		extensions.add(builder)
	}

	/** Configure the built-in help extension, or disable it so you can use your own. **/
	public open suspend fun help(builder: HelpExtensionBuilder.() -> Unit) {
		builder(helpExtensionBuilder)
	}

	/** Configure the built-in sentry extension, or disable it so you can use your own. **/
	public open suspend fun sentry(builder: SentryExtensionBuilder.() -> Unit) {
		builder(sentryExtensionBuilder)
	}
}
