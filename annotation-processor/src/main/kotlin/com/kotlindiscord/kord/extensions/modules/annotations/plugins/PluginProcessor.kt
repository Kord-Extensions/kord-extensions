/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.modules.annotations.plugins

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.google.devtools.ksp.validate
import java.util.*

/** Annotation processor for processing wired plugin "plugins". **/
public class PluginProcessor(
	private val generator: CodeGenerator,
	private val logger: KSPLogger,
) : SymbolProcessor {
	override fun process(resolver: Resolver): List<KSAnnotated> {
		val plugins = resolver.getSymbolsWithAnnotation(
			"com.kotlindiscord.kord.extensions.plugins.annotations.plugins.WiredPlugin"
		)

		val ret = plugins
			.filter { !it.validate() }
			.toList()

		ret.forEach {
			logger.warn("Unable to validate: $it")
		}

		val toProcess = plugins
			.filterIsInstance<KSClassDeclaration>()
			.filter { symbol ->
				symbol.validate() &&
					symbol.superTypes
						.mapNotNull { it.resolve().declaration as? KSClassDeclaration }
						.mapNotNull { it.qualifiedName?.asString() }
						.contains("com.kotlindiscord.kord.extensions.plugins.KordExPlugin")
			}.toList()

		val wrongSuperType = plugins
			.filterIsInstance<KSClassDeclaration>()
			.filter { symbol ->
				!(
					symbol.validate() &&
						symbol.superTypes
							.mapNotNull { it.resolve().declaration as? KSClassDeclaration }
							.mapNotNull { it.qualifiedName?.asString() }
							.contains("com.kotlindiscord.kord.extensions.plugins.KordExPlugin")
					)
			}.toList()

		wrongSuperType.forEach {
			logger.error(
				"Annotated class does not extend KordExPlugin: ${it.qualifiedName?.asString()}"
			)
		}

		if (toProcess.size > 1) {
			logger.error(
				"This project contains more than one wired plugin. Only the first one will be processed."
			)
		}

		toProcess.firstOrNull()?.accept(PluginVisitor(), Unit)

		return ret
	}

	/** Visitor for plugin classes. **/
	public inner class PluginVisitor : KSVisitorVoid() {
		override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
			val annotation = classDeclaration.annotations.filter {
				it.annotationType.resolve().declaration.qualifiedName?.asString() ==
					"com.kotlindiscord.kord.extensions.plugins.annotations.plugins.WiredPlugin"
			}.first()

			val file = generator.createNewFile(
				Dependencies(true, classDeclaration.containingFile!!),
				"META-INF", "plugin", "properties"
			)

			val props = Properties()
			val args = PluginArgs(annotation)

			props["plugin.class"] = classDeclaration.qualifiedName!!.asString()
			props["plugin.id"] = args.id
			props["plugin.version"] = args.version

			if (args.dependencies.isNotEmpty()) {
				props["plugin.dependencies"] = args.dependencies.joinToString()
			}

			args.kordExVersion?.let {
				props["plugin.requires"] = it
			}

			args.description?.let {
				props["plugin.description"] = it
			}

			args.author?.let {
				props["plugin.provider"] = it
			}

			args.license?.let {
				props["plugin.license"] = it
			}

			props.store(file.writer(), "Generated plugin/\"plugin\" definition.")
		}
	}
}
