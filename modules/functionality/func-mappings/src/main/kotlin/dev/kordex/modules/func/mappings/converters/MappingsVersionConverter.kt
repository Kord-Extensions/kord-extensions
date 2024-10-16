/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

@file:OptIn(DelicateCoroutinesApi::class, ExperimentalCoroutinesApi::class)

package dev.kordex.modules.func.mappings.converters

import dev.kord.core.entity.interaction.OptionValue
import dev.kord.core.entity.interaction.StringOptionValue
import dev.kord.rest.builder.interaction.StringChoiceBuilder
import dev.kordex.core.DiscordRelayedException
import dev.kordex.core.annotations.converters.Converter
import dev.kordex.core.annotations.converters.ConverterType
import dev.kordex.core.commands.Argument
import dev.kordex.core.commands.CommandContext
import dev.kordex.core.commands.OptionWrapper
import dev.kordex.core.commands.converters.SingleConverter
import dev.kordex.core.commands.converters.Validator
import dev.kordex.core.commands.wrapOption
import dev.kordex.core.i18n.types.Key
import dev.kordex.core.i18n.withContext
import dev.kordex.modules.func.mappings.i18n.generated.MappingsTranslations
import dev.kordex.parser.StringParser
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.withContext
import me.shedaniel.linkie.MappingsContainer
import me.shedaniel.linkie.Namespace

/**
 * Argument converter for [MappingsContainer] objects based on mappings versions.
 */
@Converter(
	"mappingsVersion",
	types = [ConverterType.SINGLE, ConverterType.OPTIONAL],
	imports = ["me.shedaniel.linkie.Namespace"],

	builderFields = ["public lateinit var namespaceGetter: suspend () -> Namespace"],
	builderExtraStatements = [
		"/** Convenience function for setting the namespace getter to a specific namespace. **/",
		"public fun namespace(namespace: Namespace) {",
		"    namespaceGetter = { namespace }",
		"}",
	]
)
class MappingsVersionConverter(
	private val namespaceGetter: suspend () -> Namespace,
	override var validator: Validator<MappingsContainer> = null,
) : SingleConverter<MappingsContainer>() {
	override val signatureType: Key = MappingsTranslations.Converter.Version.type
	override val showTypeInSignature: Boolean = false

	override suspend fun parse(parser: StringParser?, context: CommandContext, named: String?): Boolean {
		val arg: String = named ?: parser?.parseNext()?.data ?: return false

		return parse(arg, context)
	}

	override suspend fun toSlashOption(arg: Argument<*>): OptionWrapper<StringChoiceBuilder> =
		wrapOption(arg.displayName, arg.description) {
			required = true
		}

	override suspend fun parseOption(context: CommandContext, option: OptionValue<*>): Boolean {
		val optionValue: String = (option as? StringOptionValue)?.value ?: return false

		return parse(optionValue, context)
	}

	private suspend fun parse(string: String, commandContext: CommandContext): Boolean {
		newSingleThreadContext("version-parser").use { context ->
			return withContext(context) {
				val namespace: Namespace = namespaceGetter.invoke()

				if (string in namespace.getAllVersions()) {
					val version: MappingsContainer? = namespace.getProvider(string).getOrNull()

					if (version != null) {
						this@MappingsVersionConverter.parsed = version

						return@withContext true
					}
				}

				throw DiscordRelayedException(
					MappingsTranslations.Response.Error.invalidNamespaceVersion
						.withContext(commandContext)
						.withNamedPlaceholders(
							"namespace" to namespace.id,
							"version" to string
						)
				)
			}
		}
	}
}
