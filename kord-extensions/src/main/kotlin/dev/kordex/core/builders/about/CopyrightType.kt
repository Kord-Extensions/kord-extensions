/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.builders.about

import dev.kordex.core.i18n.generated.CoreTranslations
import dev.kordex.core.i18n.types.Key

public sealed class CopyrightType(public val key: Key) {
	public object Framework : CopyrightType(CoreTranslations.Extensions.About.Copyright.Type.frameworks)
	public object Library : CopyrightType(CoreTranslations.Extensions.About.Copyright.Type.libraries)
	public object PluginModule : CopyrightType(CoreTranslations.Extensions.About.Copyright.Type.plugins_modules)
	public object Tool : CopyrightType(CoreTranslations.Extensions.About.Copyright.Type.tools)
}
