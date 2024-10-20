/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.modules.func.mappings.utils

import dev.kordex.core.i18n.types.Key
import dev.kordex.modules.func.mappings.i18n.generated.MappingsTranslations

enum class QueryType(val readableName: String, val singular: Key, val plural: Key) {
	CLASS("class", MappingsTranslations.Query.Class.singular, MappingsTranslations.Query.Class.plural),
	FIELD("field", MappingsTranslations.Query.Field.singular, MappingsTranslations.Query.Field.plural),
	METHOD("method", MappingsTranslations.Query.Method.singular, MappingsTranslations.Query.Method.plural),
}
