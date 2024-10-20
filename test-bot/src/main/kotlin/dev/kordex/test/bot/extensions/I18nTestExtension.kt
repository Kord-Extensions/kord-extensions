/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

@file:Suppress("StringLiteralDuplication")

package dev.kordex.test.bot.extensions

import dev.kord.common.asJavaLocale
import dev.kord.core.behavior.interaction.suggestString
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kordex.core.annotations.NotTranslated
import dev.kordex.core.checks.types.CheckContextWithCache
import dev.kordex.core.checks.userFor
import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.application.slash.group
import dev.kordex.core.commands.application.slash.publicSubCommand
import dev.kordex.core.commands.converters.impl.int
import dev.kordex.core.commands.converters.impl.string
import dev.kordex.core.extensions.Extension
import dev.kordex.core.extensions.ephemeralSlashCommand
import dev.kordex.core.extensions.publicSlashCommand
import dev.kordex.core.i18n.toKey
import dev.kordex.core.i18n.types.Bundle
import dev.kordex.test.bot.Translations

@OptIn(NotTranslated::class)
public class I18nTestExtension : Extension() {
	override val name: String = "kordex.test-i18n"

	override suspend fun setup() {
		publicSlashCommand {
			name = Translations.Command.bananaFlat
			description = "Translated banana".toKey()

			action {
				val commandLocale = getLocale()
				val interactionLocale = event.interaction.locale?.asJavaLocale()

				assert(commandLocale == interactionLocale) {
					"Command locale (`$commandLocale`) does not match interaction locale (`$interactionLocale`)"
				}

				respond {
					content = "Text: ${Translations.Command.banana.translateLocale(getLocale())}"
				}
			}
		}

		publicSlashCommand {
			name = Translations.Command.bananaSub
			description = "Translated banana subcommand".toKey()

			publicSubCommand {
				name = Translations.Command.banana
				description = "Translated banana".toKey()

				action {
					val commandLocale = getLocale()
					val interactionLocale = event.interaction.locale?.asJavaLocale()

					assert(commandLocale == interactionLocale) {
						"Command locale (`$commandLocale`) does not match interaction locale (`$interactionLocale`)"
					}

					respond {
						content = "Text: ${Translations.Command.banana.translateLocale(getLocale())}"
					}
				}
			}
		}

		publicSlashCommand {
			name = Translations.Command.bananaGroup
			description = "Translated banana group".toKey()

			group(Translations.Command.banana) {
				description = "Translated banana group".toKey()

				publicSubCommand {
					name = Translations.Command.banana
					description = "Translated banana".toKey()

					action {
						val commandLocale = getLocale()
						val interactionLocale = event.interaction.locale?.asJavaLocale()

						assert(commandLocale == interactionLocale) {
							"Command locale (`$commandLocale`) does not match interaction locale (`$interactionLocale`)"
						}

						respond {
							content = "Text: ${Translations.Command.banana.translateLocale(getLocale())}"
						}
					}
				}
			}
		}

		publicSlashCommand(::I18nTestArguments) {
			name = Translations.Command.fruit
			description = Translations.Command.fruit

			action {
				respond {
					content = Translations.Command.Fruit.response
						.withLocale(getLocale())
						.translate(arguments.fruit)
				}
			}
		}

		publicSlashCommand(::I18nTestNamedArguments) {
			name = Translations.Command.apple
			description = Translations.Command.apple

			action {
				respond {
					content = Translations.Command.Apple.response
						.withLocale(getLocale())
						.translateNamed(
							"name" to arguments.name,
							"appleCount" to arguments.count
						)
				}
			}
		}

		ephemeralSlashCommand {
			name = "test-translated-checks".toKey()
			description = "Command that always fails, to check CheckContext translations.".toKey()

			check {
				translatedChecks()
			}

			action {
				// This command is expected to always fail, in order to test checks.
				respond {
					content = "It is impossible to get here."
				}
			}
		}

		ephemeralSlashCommand(::I18nTestValidations) {
			name = "test-translated-validations".toKey()
			description = "Command with arguments that always fail validations.".toKey()

			action {
				// This command is expected to always fail, in order to test argument validations.
				respond {
					content = "It is impossible to get here."
				}
			}
		}
	}

	private suspend fun CheckContextWithCache<ChatInputCommandInteractionCreateEvent>.translatedChecks() {
		val user = userFor(event)

		if (user == null) {
			fail("Could not get user.".toKey())
			return
		}

		fail(
			buildList {
				// Translate, with default bundle
				add(
					Translations.Check.simple
						.withLocale(locale)
						.translate()
				)

				// Translate with a different bundle
				add(
					Translations.Check.simple
						.withBundle(Bundle("custom"))
						.withLocale(locale)
						.translate()
				)

				// Translate with default bundle, and positional parameters
				add(
					Translations.Check.positionalParameters
						.withLocale(locale)
						.translate(user.mention, user.id)
				)

				// Translate with a different bundle, and positional parameters
				add(
					Translations.Check.positionalParameters
						.withBundle(Bundle("custom"))
						.withLocale(locale)
						.translate(user.mention, user.id)
				)

				// Translate with default bundle, named parameters
				add(
					Translations.Check.namedParameters
						.withLocale(locale)
						.translateNamed("user" to user.mention, "id" to user.id)
				)

				// Translate with a different bundle, and named parameters
				add(
					Translations.Check.namedParameters
						.withBundle(Bundle("custom"))
						.withLocale(locale)
						.translateNamed("user" to user.mention, "id" to user.id)
				)
			}.joinToString("\n")
		)
	}

	private inner class I18nTestValidations : Arguments() {
		val name by string {
			name = "name".toKey()
			description = "Will always fail to validate.".toKey()

			validate {
				val locale = context.getLocale()

				fail(
					buildList {
						// Translate, with default bundle
						add(
							Translations.Validation.simple
								.withLocale(locale)
								.translate()
						)

						// Translate with a different bundle
						add(
							Translations.Validation.simple
								.withBundle(Bundle("custom"))
								.withLocale(locale)
								.translate()
						)

						// Translate with default bundle, and positional parameters
						add(
							Translations.Validation.positionalParameters
								.withLocale(locale)
								.translate(value)
						)

						// Translate with a different bundle, and positional parameters
						add(
							Translations.Validation.positionalParameters
								.withBundle(Bundle("custom"))
								.withLocale(locale)
								.translate(value)
						)

						// Translate with default bundle, named parameters
						add(
							Translations.Validation.namedParameters
								.withLocale(locale)
								.translateNamed("value" to value)
						)

						// Translate with a different bundle, and named parameters
						add(
							Translations.Validation.namedParameters
								.withBundle(Bundle("custom"))
								.withLocale(locale)
								.translateNamed("value" to value)
						)
					}.joinToString("\n")
				)
			}
		}
	}
}

internal class I18nTestArguments : Arguments() {
	val fruit by string {
		name = Translations.Command.fruit
		description = Translations.Command.fruit

		autoComplete {
			suggestString {
				listOf("Banana", "Apple", "Cherry").forEach { choice(it, it) }
			}
		}
	}
}

internal class I18nTestNamedArguments : Arguments() {
	val name by string {
		name = Translations.Command.Apple.Argument.name
		description = Translations.Command.Apple.Argument.name
	}

	val count by int {
		name = Translations.Command.Apple.Argument.count
		description = Translations.Command.Apple.Argument.count
	}
}
