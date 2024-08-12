/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.events.extra.models

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("field_type")
public sealed class GuildJoinRequestResponse {
	public abstract val required: Boolean
	public abstract val label: String

	// description, automations?

	@Serializable
	@SerialName("TERMS")
	public class TermsResponse(
		override val required: Boolean,
		override val label: String,

		public val response: Boolean,
		public val values: List<String>,
	) : GuildJoinRequestResponse()

	@Serializable
	@SerialName("PARAGRAPH")
	public class ParagraphResponse(
		override val required: Boolean,
		override val label: String,

		public val placeholder: String?,
		public val response: String,
	) : GuildJoinRequestResponse()

	@Serializable
	@SerialName("MULTIPLE_CHOICE")
	public class MultipleChoiceResponse(
		override val required: Boolean,
		override val label: String,

		public val response: Int,
		public val choices: List<String>,
	) : GuildJoinRequestResponse()
}
