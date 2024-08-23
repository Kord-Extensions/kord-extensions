/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.builders.about

public sealed class CopyrightType(public val key: String) {
	public object Framework : CopyrightType("extensions.about.copyright.type.frameworks")
	public object Library : CopyrightType("extensions.about.copyright.type.libraries")
	public object PluginModule : CopyrightType("extensions.about.copyright.type.plugins-modules")
	public object Tool : CopyrightType("extensions.about.copyright.type.tools")
}
