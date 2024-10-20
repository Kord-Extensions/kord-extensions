/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.test.bot

import dev.kordex.core.i18n.types.Bundle
import dev.kordex.core.i18n.types.Key

/** Written by hand for the sake of these tests. **/
internal object Translations {
	val bundle = Bundle("test.strings")

	object Check {
		val simple = Key("check.simple")
			.withBundle(bundle)

		val positionalParameters = Key("check.positionalParameters")
			.withBundle(bundle)

		val namedParameters = Key("check.namedParameters")
			.withBundle(bundle)
	}

	object Command {
		object Fruit {
			object Argument {
				val name = Key("command.fruit.argument.name")
					.withBundle(bundle)

				val count = Key("command.fruit.argument.count")
					.withBundle(bundle)
			}

			val response = Key("command.fruit.response")
				.withBundle(bundle)
		}

		object Apple {
			object Argument {
				val name = Key("command.apple.argument.name")
					.withBundle(bundle)

				val count = Key("command.apple.argument.count")
					.withBundle(bundle)
			}

			val response = Key("command.apple.response")
				.withBundle(bundle)
		}

		val apple = Key("command.apple")
			.withBundle(bundle)

		val banana = Key("command.banana")
			.withBundle(bundle)

		val bananaFlat = Key("command.banana-flat")
			.withBundle(bundle)

		val bananaSub = Key("command.banana-sub")
			.withBundle(bundle)

		val bananaGroup = Key("command.banana-group")
			.withBundle(bundle)

		val fruit = Key("command.fruit")
			.withBundle(bundle)
	}

	object Modal {
		object Line {
			val placeholder = Key("modal.line.placeholder")
				.withBundle(bundle)
		}

		object Paragraph {
			val placeholder = Key("modal.paragraph.placeholder")
				.withBundle(bundle)
		}

		val title = Key("modal.title")
			.withBundle(bundle)

		val line = Key("modal.line")
			.withBundle(bundle)

		val paragraph = Key("modal.paragraph")
			.withBundle(bundle)
	}

	object Validation {
		val simple = Key("validation.simple")
			.withBundle(bundle)

		val positionalParameters = Key("validation.positionalParameters")
			.withBundle(bundle)

		val namedParameters = Key("validation.namedParameters")
			.withBundle(bundle)
	}
}
