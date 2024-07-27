/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
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
