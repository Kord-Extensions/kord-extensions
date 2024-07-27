/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

@file:OptIn(DelicateCoroutinesApi::class, ExperimentalCoroutinesApi::class)

package dev.kordex.modules.func.mappings.converters

import dev.kord.core.entity.interaction.OptionValue
import dev.kord.core.entity.interaction.StringOptionValue
import dev.kord.rest.builder.interaction.OptionsBuilder
import dev.kord.rest.builder.interaction.StringChoiceBuilder
import dev.kordex.core.DiscordRelayedException
import dev.kordex.core.annotations.converters.Converter
import dev.kordex.core.annotations.converters.ConverterType
import dev.kordex.core.commands.Argument
import dev.kordex.core.commands.CommandContext
import dev.kordex.core.commands.converters.SingleConverter
import dev.kordex.core.commands.converters.Validator
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
	override val signatureTypeString: String = "version"
	override val showTypeInSignature: Boolean = false

	override suspend fun parse(parser: StringParser?, context: CommandContext, named: String?): Boolean {
		val arg: String = named ?: parser?.parseNext()?.data ?: return false
		return parse(arg)
	}

	override suspend fun toSlashOption(arg: Argument<*>): OptionsBuilder =
		StringChoiceBuilder(arg.displayName, arg.description).apply { required = true }

	override suspend fun parseOption(context: CommandContext, option: OptionValue<*>): Boolean {
		val optionValue: String = (option as? StringOptionValue)?.value ?: return false
		return parse(optionValue)
	}

	private suspend fun parse(string: String): Boolean {
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

				throw DiscordRelayedException("Invalid ${namespace.id} version: `$string`")
			}
		}
	}
}
