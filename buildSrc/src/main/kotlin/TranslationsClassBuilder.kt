import com.hanggrian.kotlinpoet.TypeSpecBuilder
import com.hanggrian.kotlinpoet.addObject
import com.hanggrian.kotlinpoet.buildFileSpec
import com.hanggrian.kotlinpoet.buildPropertySpec
import com.squareup.kotlinpoet.ClassName
import org.gradle.configurationcache.extensions.capitalized
import java.util.Properties

/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

fun bundle(bundleName: String) =
	buildPropertySpec("bundle", ClassName("dev.kordex.core.i18n.types", "Bundle")) {
		setInitializer("Bundle(%S)", bundleName)
	}

fun key(name: String, value: String, property: String, translationsClassName: String) =
	buildPropertySpec(name.replace("-", "_"), ClassName("dev.kordex.core.i18n.types", "Key")) {
		setInitializer("Key(%S)\n.withBundle(%L.bundle)", value, translationsClassName)

		property.lines().forEach {
			kdoc.addStatement(
				"%L",
				it.trim().replace("*/", "* /")
			)
		}
	}

fun createTranslationsClass(
	classPackage: String,
	keys: List<String>,
	props: Properties,
	bundleName: String,
	translationsClassName: String,
) = buildFileSpec(classPackage, translationsClassName) {
	types.addObject(translationsClassName) {
		properties.add(
			bundle(bundleName)
		)

		addKeys(keys, props, translationsClassName)
	}
}

fun TypeSpecBuilder.addKeys(
	keys: List<String>,
	props: Properties,
	translationsClassName: String,
	parent: String? = null,
) {
	val paritioned = keys.partition()

	paritioned.forEach { (k, v) ->
		val keyName = if (parent != null) {
			"$parent.$k"
		} else {
			k
		}

		if (v.isEmpty() || props[keyName] != null) {
			properties.add(
				key(k.toVarName(), keyName, props.getProperty(keyName), translationsClassName)
			)
		}

		if (v.isNotEmpty()) {
			// Object
			val objName = k
				.replace("-", " ")
				.split(" ")
				.map { it.capitalized() }
				.joinToString("")

			types.addObject(objName) {
				addKeys(v, props, translationsClassName, keyName)
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
