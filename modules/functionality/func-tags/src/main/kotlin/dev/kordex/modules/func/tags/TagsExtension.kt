/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

@file:Suppress("StringLiteralDuplication")

package dev.kordex.modules.func.tags

import dev.kord.common.annotation.KordUnsafe
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.interaction.modal
import dev.kord.core.behavior.interaction.suggestString
import dev.kord.rest.builder.message.EmbedBuilder.Limits.title
import dev.kord.rest.builder.message.allowedMentions
import dev.kordex.core.checks.anyGuild
import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.application.slash.ephemeralSubCommand
import dev.kordex.core.commands.converters.impl.*
import dev.kordex.core.components.forms.ModalForm
import dev.kordex.core.extensions.Extension
import dev.kordex.core.extensions.ephemeralSlashCommand
import dev.kordex.core.extensions.publicSlashCommand
import dev.kordex.core.utils.FilterStrategy
import dev.kordex.core.utils.suggestStringMap
import dev.kordex.modules.dev.unsafe.annotations.UnsafeAPI
import dev.kordex.modules.dev.unsafe.extensions.unsafeSubCommand
import dev.kordex.modules.dev.unsafe.types.InitialSlashCommandResponse
import dev.kordex.modules.dev.unsafe.types.ackEphemeral
import dev.kordex.modules.dev.unsafe.types.respondEphemeral
import dev.kordex.modules.func.tags.config.TagsConfig
import dev.kordex.modules.func.tags.data.Tag
import dev.kordex.modules.func.tags.data.TagsData
import org.koin.core.component.inject

internal const val POSITIVE_EMOTE = "\uD83D\uDC4D"
internal const val NEGATIVE_EMOTE = "❌"

@OptIn(UnsafeAPI::class)
@Suppress("MagicNumber")
class TagsExtension : Extension() {
	override val name: String = "ext-tags"

	val tagsConfig: TagsConfig by inject()
	val tagsData: TagsData by inject()

	override suspend fun setup() {
		publicSlashCommand(::GetTagArgs) {
			name = "tag"
			description = "Retrieve a tag and send it"

			tagsConfig.getUserCommandChecks().forEach(::check)

			action {
				val tag = tagsData.getTagByKey(arguments.tagKey, guild?.id)

				if (tag == null) {
					respond {
						content = "$NEGATIVE_EMOTE Unknown tag: ${arguments.tagKey}"
					}

					return@action
				}

				respond {
					tagsConfig.getTagFormatter()
						.invoke(this, tag)

					if (arguments.userToMention != null) {
						content = "${arguments.userToMention!!.mention}\n\n${content ?: ""}"

						allowedMentions {
							users += arguments.userToMention!!.id
						}
					}
				}
			}
		}

		ephemeralSlashCommand {
			name = "list-tags"
			description = "Commands for listing tags by various criteria"

			tagsConfig.getUserCommandChecks().forEach(::check)

			ephemeralSubCommand(::ByCategoryArgs) {
				name = "by-category"
				description = "List tags by matching their category"

				action {
					val tags = tagsData.getTagsByCategory(arguments.category, guild?.id)

					if (tags.isEmpty()) {
						respond {
							content = "$NEGATIVE_EMOTE Tag not found"
						}

						return@action
					}

					editingPaginator {
						timeoutSeconds = 60

						tags.forEach { tag ->
							page {
								title = tag.title
								description = tag.description
								color = tag.color

								footer {
									text = "${tag.category}/${tag.key}"
								}

								image = tag.image
							}
						}
					}.send()
				}
			}

			ephemeralSubCommand(TagsExtension::ByKeyArgs) {
				name = "by-key"
				description = "List tags by matching their key"

				action {
					val tags = tagsData.getTagsByPartialKey(arguments.key, guild?.id)

					if (tags.isEmpty()) {
						respond {
							content = "$NEGATIVE_EMOTE Tag not found"
						}

						return@action
					}

					editingPaginator {
						timeoutSeconds = 60

						tags.forEach { tag ->
							page {
								title = tag.title
								description = tag.description
								color = tag.color

								footer {
									text = "${tag.category}/${tag.key}"
								}

								image = tag.image
							}
						}
					}.send()
				}
			}

			ephemeralSubCommand(TagsExtension::ByTitleArgs) {
				name = "by-title"
				description = "List tags by matching their title"

				action {
					val tags = tagsData.getTagsByPartialTitle(arguments.title, guild?.id)

					if (tags.isEmpty()) {
						respond {
							content = "$NEGATIVE_EMOTE Tag not found"
						}

						return@action
					}

					editingPaginator {
						timeoutSeconds = 60

						tags.forEach { tag ->
							page {
								title = tag.title
								description = tag.description
								color = tag.color

								footer {
									text = "${tag.category}/${tag.key}"
								}

								image = tag.image
							}
						}
					}.send()
				}
			}
		}

		ephemeralSlashCommand {
			name = "manage-tags"
			description = "Tag management commands"

			allowInDms = false

			check {
				anyGuild()
			}

			tagsConfig.getStaffCommandChecks().forEach(::check)

			@OptIn(KordUnsafe::class)
			unsafeSubCommand(::SetArgs) {
				name = "set"
				description = "Create or replace a tag"

				initialResponse = InitialSlashCommandResponse.None

				action {
					val modalObj = TagEditModal()

					this@unsafeSubCommand.componentRegistry.register(modalObj)

					event.interaction.modal(
						modalObj.translateTitle(getLocale(), bundle),
						modalObj.id
					) {
						modalObj.applyToBuilder(this, getLocale(), bundle)
					}

					interactionResponse = modalObj.awaitCompletion {
						it?.deferEphemeralResponseUnsafe()
					} ?: return@action

					val tag = Tag(
						category = arguments.category,
						description = modalObj.description.value!!,
						key = arguments.key,
						title = modalObj.tagTitle.value!!,
						color = arguments.colour,
						guildId = arguments.guild?.id,
						image = modalObj.imageUrl.value.nullIfBlank()
					)

					tagsData.setTag(tag)

					tagsConfig.getLoggingChannel(guild!!.asGuild()).createMessage {
						allowedMentions { }

						content = "**Tag created/updated by ${user.mention}**\n\n"

						tagsConfig.getTagFormatter().invoke(this, tag)
					}

					respondEphemeral {
						content = "$POSITIVE_EMOTE Tag set: ${tag.title}"
					}
				}
			}

			@OptIn(KordUnsafe::class)
			unsafeSubCommand(::EditArgs) {
				name = "edit"
				description = "Edit an existing tag"

				initialResponse = InitialSlashCommandResponse.None

				action {
					var tag = tagsData.getTagByKey(arguments.key, arguments.guild?.id)

					if (tag == null) {
						ackEphemeral {
							content = "$NEGATIVE_EMOTE Tag not found"
						}

						return@action
					}

					val modalObj = TagEditModal(
						true,
						tag.key,
						tag.title,
						tag.description,
						tag.image
					)

					this@unsafeSubCommand.componentRegistry.register(modalObj)

					event.interaction.modal(
						modalObj.translateTitle(getLocale(), bundle),
						modalObj.id
					) {
						modalObj.applyToBuilder(this, getLocale(), bundle)
					}

					interactionResponse = modalObj.awaitCompletion {
						it?.deferEphemeralResponseUnsafe()
					} ?: return@action

					if (!modalObj.tagTitle.value.isNullOrBlank()) {
						tag = tag.copy(title = modalObj.tagTitle.value!!)
					}

					if (!modalObj.description.value.isNullOrBlank()) {
						tag = tag.copy(description = modalObj.description.value!!)
					}

					if (arguments.category != null) {
						tag = tag.copy(category = arguments.category!!)
					}

					if (arguments.colour != null) {
						tag = tag.copy(color = arguments.colour!!)
					}

					if (modalObj.imageUrl.value.isNullOrBlank()) {
						tag = tag.copy(image = null)
					} else {
						tag = tag.copy(image = modalObj.imageUrl.value)
					}

					tagsData.setTag(tag)

					tagsConfig.getLoggingChannel(guild!!.asGuild()).createMessage {
						allowedMentions { }

						content = "**Tag edited by ${user.mention}**\n\n"

						tagsConfig.getTagFormatter().invoke(this, tag)
					}

					respondEphemeral {
						content = "$POSITIVE_EMOTE Tag edited: ${tag.title}"
					}
				}
			}

			ephemeralSubCommand(TagsExtension::FindArgs) {
				name = "find"
				description = "Find tags, by the given key and guild ID"

				action {
					val tags = tagsData.findTags(
						category = arguments.category,
						guildId = arguments.guild?.id,
						key = arguments.key
					)

					if (tags.isEmpty()) {
						respond {
							content = "$NEGATIVE_EMOTE No tags found for that query."
						}

						return@action
					}

					editingPaginator {
						timeoutSeconds = 60

						val chunks = tags.chunked(10)

						chunks.forEach { chunk ->
							page {
								description = chunk.joinToString("\n\n") {
									"""
                                        **Key:** `${it.key}`
                                        **Title:** `${it.title}`
                                        **Category:** `${it.category}`
                                        **Guild ID:** `${it.guildId ?: "N/A"}`
                                        **Image:** `${it.image ?: "N/A"}`
                                    """.trimIndent()
								}
							}
						}
					}.send()
				}
			}

			ephemeralSubCommand(TagsExtension::ByKeyAndOptionalGuildArgs) {
				name = "delete"
				description = "Delete a tag, by key and guild ID"

				action {
					val tag = tagsData.deleteTagByKey(arguments.key, arguments.guild?.id)

					if (tag != null) {
						tagsConfig.getLoggingChannel(guild!!.asGuild()).createMessage {
							allowedMentions { }

							content = "**Tag removed by ${user.mention}**\n\n"

							tagsConfig.getTagFormatter().invoke(this, tag)
						}
					}

					respond {
						content = if (tag == null) {
							"$NEGATIVE_EMOTE Tag not found"
						} else {
							"$POSITIVE_EMOTE Deleted tag: ${tag.title}"
						}
					}
				}
			}
		}
	}

	// region: Arguments

	private fun GetTagArgs(): GetTagArgs =
		GetTagArgs(tagsData)

	private fun ByCategoryArgs(): ByCategoryArgs =
		ByCategoryArgs(tagsData)

	private fun SetArgs(): SetArgs =
		SetArgs(tagsData)

	private fun EditArgs(): EditArgs =
		EditArgs(tagsData)

	internal class ByKeyAndOptionalGuildArgs : Arguments() {
		val key by string {
			name = "key"
			description = "Tag key to match by"
		}

		val guild by optionalGuild {
			name = "guild"
			description = "Optional guild to match by - \"this\" for the current guild"
		}
	}

	internal class FindArgs : Arguments() {
		val category by optionalString {
			name = "category"
			description = "Optional category to match by"
		}

		val key by optionalString {
			name = "key"
			description = "Optional tag key to match by"
		}

		val guild by optionalGuild {
			name = "guild"
			description = "Optional guild to match by - \"this\" for the current guild"
		}
	}

	internal class SetArgs(tagsData: TagsData) : Arguments() {
		val key by string {
			name = "key"
			description = "Unique tag key"
		}

		val category by string {
			name = "category"
			description = "Category to use for this tag - specify a new one to create it"

			autoComplete {
				val categories = tagsData.getAllCategories(data.guildId.value)

				suggestStringMap(
					categories.associateWith { it },
					FilterStrategy.Contains
				)
			}
		}

		val colour by optionalColor {
			name = "colour"
			description = "Optional embed colour - use hex codes, RGB integers or Discord colour constants"
		}

		val guild by optionalGuild {
			name = "guild"
			description = "Optional guild to limit the tag to - \"this\" for the current guild"
		}
	}

	internal class EditArgs(tagsData: TagsData) : Arguments() {
		val key by string {
			name = "key"
			description = "Tag key to use for matching (this can't be edited)"
		}

		val guild by optionalGuild {
			name = "guild"
			description = "Optional guild to use for matching (this can't be edited)"
		}

		val category by optionalString {
			name = "category"
			description = "Category to use for this tag - specify a new one to create it"

			autoComplete {
				val categories = tagsData.getAllCategories(data.guildId.value)

				suggestStringMap(
					categories.associateWith { it },
					FilterStrategy.Contains
				)
			}
		}

		val colour by optionalColor {
			name = "colour"
			description = "Use hex codes, RGB integers (0 to clear) or Discord colour constants"
		}
	}

	internal class ByCategoryArgs(tagsData: TagsData) : Arguments() {
		val category by string {
			name = "category"
			description = "Category to match by"

			autoComplete {
				val categories = tagsData.getAllCategories(data.guildId.value)

				suggestStringMap(
					categories.associateWith { it },
					FilterStrategy.Contains
				)
			}
		}
	}

	internal class TagEditModal(
		isEditing: Boolean = false,
		key: String? = null,
		private val initialTagTitle: String? = null,
		private val initialDescription: String? = null,
		private val initialImageUrl: String? = null,
	) : ModalForm() {
		override var title: String = if (!isEditing) {
			"Create tag"
		} else {
			"Edit tag"
		} + if (key != null) {
			": $key"
		} else {
			""
		}

		val tagTitle = lineText {
			label = "Title"
			initialValue = initialTagTitle
		}

		val description = paragraphText {
			label = "Tag content"
			initialValue = initialDescription
		}

		val imageUrl = lineText {
			label = "Image URL"
			initialValue = initialImageUrl

			required = false
		}
	}

	internal class ByKeyArgs : Arguments() {
		val key by string {
			name = "key"
			description = "Partial key to match by"
		}
	}

	internal class ByTitleArgs : Arguments() {
		val title by string {
			name = "title"
			description = "Partial title to match by"
		}
	}

	internal class GetTagArgs(tagsData: TagsData) : Arguments() {
		val tagKey by string {
			name = "tag"
			description = "Tag to retrieve"

			autoComplete {
				val input = focusedOption.value

				var category: String? = null
				lateinit var tagKey: String

				if ("/" in input) {
					category = input.substringBeforeLast("/")
					tagKey = input.substringAfterLast("/")
				} else {
					tagKey = input
				}

				var potentialTags = (
					tagsData.getTagsByPartialKey(tagKey, this.data.guildId.value) +
						tagsData.getTagsByPartialTitle(tagKey, this.data.guildId.value)
					)
					.toSet()
					.toList()

				if (category != null) {
					potentialTags = potentialTags.filter { it.category.startsWith(category!!, true) }
				}

				val foundKeys: MutableList<String> = mutableListOf()

				potentialTags = potentialTags
					.sortedBy { if (it.guildId == null) -1 else 1 }
					.filter {
						if (it.key !in foundKeys) {
							foundKeys.add(it.key)

							true
						} else {
							false
						}
					}

				potentialTags = potentialTags
					.sortedBy { it.title }
					.take(25)

				suggestString {
					potentialTags.forEach {
						choice("(${it.category}/${it.key}) ${it.title}", it.key)
					}
				}
			}
		}

		val userToMention by optionalMember {
			name = "user"
			description = "User to mention along with this tag."
		}
	}

	// endregion
}
