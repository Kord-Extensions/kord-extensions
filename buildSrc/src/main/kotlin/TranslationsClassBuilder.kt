import com.hanggrian.kotlinpoet.TypeSpecBuilder
import com.hanggrian.kotlinpoet.addObject
import com.hanggrian.kotlinpoet.buildFileSpec
import com.hanggrian.kotlinpoet.buildObjectTypeSpec
import com.hanggrian.kotlinpoet.buildPropertySpec
import com.squareup.kotlinpoet.ClassName
import org.gradle.configurationcache.extensions.capitalized

/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

fun key(name: String, value: String) = buildPropertySpec(name, ClassName("dev.kordex.core.i18n.types", "Key")) {
	setInitializer("Key(%S)", value)
}


fun createTranslationsClass(classPackage: String, keys: List<String>) =
	buildFileSpec(classPackage, "Translations") {
		types.addObject("Translations") {
			addKeys(keys)
		}
	}

fun TypeSpecBuilder.addKeys(keys: List<String>, parent: String? = null) {
	val paritioned = keys.partition()

	paritioned.forEach { (k, v) ->
		val keyName = if (parent != null) {
			"$parent.$k"
		} else {
			k
		}

		if (v.isEmpty()) {
			properties.add(key(k.toVarName(), keyName))
		} else {
			// Object
			types.addObject(k.capitalized()) {
				addKeys(v, keyName)
			}
		}
	}
}

fun List<String>.partition(): Map<String, List<String>> =
	filterNotNull()
		.groupBy(
			keySelector = { it.substringBefore(".") },

			valueTransform = {
				if ("." in it) {
					it.substringAfter(".")
				} else {
					null
				}
			},
		)
		.mapValues { (_, value) ->
			value.filterNotNull()
		}

fun String.toVarName() =
	replace("-", "_")
		.replace(".", "_")
		.replaceFirstChar { it.lowercase() }
		.let {
			if (it in KEYWORDS) {
				"`$it`"
			} else {
				it
			}
		}
