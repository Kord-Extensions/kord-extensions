/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

@file:Suppress("StringLiteralDuplication")

package dev.kordex.modules.func.tags

import dev.kord.common.annotation.KordUnsafe
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.interaction.modal
import dev.kord.core.behavior.interaction.suggestString
import dev.kord.rest.builder.message.allowedMentions
import dev.kordex.core.checks.anyGuild
import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.application.slash.ephemeralSubCommand
import dev.kordex.core.commands.converters.impl.*
import dev.kordex.core.components.forms.ModalForm
import dev.kordex.core.extensions.Extension
import dev.kordex.core.extensions.ephemeralSlashCommand
import dev.kordex.core.extensions.publicSlashCommand
import dev.kordex.core.i18n.toKey
import dev.kordex.core.i18n.types.Key
import dev.kordex.core.utils.FilterStrategy
import dev.kordex.core.utils.suggestStringMap
import dev.kordex.modules.dev.unsafe.annotations.UnsafeAPI
import dev.kordex.modules.dev.unsafe.commands.slash.InitialSlashCommandResponse
import dev.kordex.modules.dev.unsafe.extensions.unsafeSubCommand
import dev.kordex.modules.func.tags.config.TagsConfig
import dev.kordex.modules.func.tags.data.Tag
import dev.kordex.modules.func.tags.data.TagsData
import dev.kordex.modules.func.tags.i18n.generated.TagsTranslations
import org.koin.core.component.inject

internal const val POSITIVE_EMOTE = "\uD83D\uDC4D"
internal const val NEGATIVE_EMOTE = "❌"

@OptIn(UnsafeAPI::class)
@Suppress("MagicNumber")
class TagsExtension : Extension() {
	override val name: String = "kordex.func-tags"

	val tagsConfig: TagsConfig by inject()
	val tagsData: TagsData by inject()

	override suspend fun setup() {
		publicSlashCommand(::GetTagArgs) {
			name = TagsTranslations.Command.Tag.name
			description = TagsTranslations.Command.Tag.description

			tagsConfig.getUserCommandChecks().forEach(::check)

			action {
				val tag = tagsData.getTagByKey(arguments.tagKey, guild?.id)

				if (tag == null) {
					respond {
						content = TagsTranslations.Response.Tag.unknown
							.withLocale(getLocale())
							.translateNamed(
								"emote" to NEGATIVE_EMOTE,
								"tag" to arguments.tagKey
							)
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
			name = TagsTranslations.Command.ListTags.name
			description = TagsTranslations.Command.ListTags.description

			tagsConfig.getUserCommandChecks().forEach(::check)

			ephemeralSubCommand(::ByCategoryArgs) {
				name = TagsTranslations.Command.ListTags.ByCategory.name
				description = TagsTranslations.Command.ListTags.ByCategory.description

				action {
					val tags = tagsData.getTagsByCategory(arguments.category, guild?.id)

					if (tags.isEmpty()) {
						respond {
							content = TagsTranslations.Response.Tag.noneFound
								.withLocale(getLocale())
								.translateNamed("emote" to NEGATIVE_EMOTE)
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
				name = TagsTranslations.Command.ListTags.ByKey.name
				description = TagsTranslations.Command.ListTags.ByKey.description

				action {
					val tags = tagsData.getTagsByPartialKey(arguments.key, guild?.id)

					if (tags.isEmpty()) {
						respond {
							content = TagsTranslations.Response.Tag.noneFound
								.withLocale(getLocale())
								.translateNamed("emote" to NEGATIVE_EMOTE)
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
				name = TagsTranslations.Command.ListTags.ByTitle.name
				description = TagsTranslations.Command.ListTags.ByTitle.description

				action {
					val tags = tagsData.getTagsByPartialTitle(arguments.title, guild?.id)

					if (tags.isEmpty()) {
						respond {
							content = TagsTranslations.Response.Tag.noneFound
								.withLocale(getLocale())
								.translateNamed("emote" to NEGATIVE_EMOTE)
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
			name = TagsTranslations.Command.ManageTags.name
			description = TagsTranslations.Command.ManageTags.description

			allowInDms = false

			check {
				anyGuild()
			}

			tagsConfig.getStaffCommandChecks().forEach(::check)

			@OptIn(KordUnsafe::class)
			unsafeSubCommand(::SetArgs) {
				name = TagsTranslations.Command.ManageTags.Set.name
				description = TagsTranslations.Command.ManageTags.Set.description

				initialResponse = dev.kordex.modules.dev.unsafe.commands.slash.InitialSlashCommandResponse.None

				action {
					val modalObj = TagEditModal()

					this@unsafeSubCommand.componentRegistry.register(modalObj)

					event.interaction.modal(
						modalObj.translateTitle(getLocale()),
						modalObj.id
					) {
						modalObj.applyToBuilder(this, getLocale())
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

						content = TagsTranslations.Logging.tagSet
							.withLocale(getLocale())
							.translateNamed(
								"user" to user.mention
							)

						tagsConfig.getTagFormatter().invoke(this, tag)
					}

					respondEphemeral {
						TagsTranslations.Response.Tag.set
							.withLocale(getLocale())
							.translateNamed(
								"emote" to POSITIVE_EMOTE,
								"tag" to tag.title
							)
					}
				}
			}

			@OptIn(KordUnsafe::class)
			unsafeSubCommand(::EditArgs) {
				name = TagsTranslations.Command.ManageTags.Edit.name
				description = TagsTranslations.Command.ManageTags.Edit.description
				initialResponse = InitialSlashCommandResponse.None

				action {
					var tag = tagsData.getTagByKey(arguments.key, arguments.guild?.id)

					if (tag == null) {
						ackEphemeral {
							content = TagsTranslations.Response.Tag.noneFound
								.withLocale(getLocale())
								.translateNamed("emote" to NEGATIVE_EMOTE)
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
						modalObj.translateTitle(getLocale()),
						modalObj.id
					) {
						modalObj.applyToBuilder(this, getLocale())
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

					tag = if (modalObj.imageUrl.value.isNullOrBlank()) {
						tag.copy(image = null)
					} else {
						tag.copy(image = modalObj.imageUrl.value)
					}

					tagsData.setTag(tag)

					tagsConfig.getLoggingChannel(guild!!.asGuild()).createMessage {
						allowedMentions { }

						content = TagsTranslations.Logging.tagEdited
							.withLocale(getLocale())
							.translateNamed(
								"user" to user.mention
							)

						tagsConfig.getTagFormatter().invoke(this, tag)
					}

					respondEphemeral {
						content = TagsTranslations.Response.Tag.edited
							.withLocale(getLocale())
							.translateNamed(
								"emote" to POSITIVE_EMOTE,
								"tag" to tag.title
							)
					}
				}
			}

			ephemeralSubCommand(TagsExtension::FindArgs) {
				name = TagsTranslations.Command.ManageTags.Find.name
				description = TagsTranslations.Command.ManageTags.Find.description

				action {
					val locale = getLocale()

					val tags = tagsData.findTags(
						category = arguments.category,
						guildId = arguments.guild?.id,
						key = arguments.key
					)

					if (tags.isEmpty()) {
						respond {
							content = TagsTranslations.Response.Tag.noneFound
								.withLocale(locale)
								.translateNamed("emote" to NEGATIVE_EMOTE)
						}

						return@action
					}

					val na = TagsTranslations.Response.Words.na
						.translateLocale(locale)

					editingPaginator {
						timeoutSeconds = 60

						val chunks = tags.chunked(10)

						chunks.forEach { chunk ->
							page {
								description = chunk.joinToString("\n\n") {
									TagsTranslations.Response.Find.chunk
										.withLocale(locale)
										.translateNamed(
											"key" to it.key,
											"title" to it.title,
											"category" to it.category,
											"serverId" to (it.guildId ?: na),
											"imageUrl" to (it.image ?: na),
										)
								}
							}
						}
					}.send()
				}
			}

			ephemeralSubCommand(TagsExtension::ByKeyAndOptionalGuildArgs) {
				name = TagsTranslations.Command.ManageTags.Delete.name
				description = TagsTranslations.Command.ManageTags.Delete.description

				action {
					val tag = tagsData.deleteTagByKey(arguments.key, arguments.guild?.id)

					if (tag != null) {
						tagsConfig.getLoggingChannel(guild!!.asGuild()).createMessage {
							allowedMentions { }

							content = TagsTranslations.Logging.tagDeleted
								.withLocale(getLocale())
								.translateNamed(
									"user" to user.mention
								)

							tagsConfig.getTagFormatter().invoke(this, tag)
						}
					}

					respond {
						content = if (tag == null) {
							TagsTranslations.Response.Tag.noneFound
								.withLocale(getLocale())
								.translateNamed("emote" to NEGATIVE_EMOTE)
						} else {
							TagsTranslations.Response.Tag.deleted
								.withLocale(getLocale())
								.translateNamed(
									"emote" to POSITIVE_EMOTE,
									"tag" to tag.title
								)
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
			name = TagsTranslations.Arguments.Get.Key.name
			description = TagsTranslations.Arguments.Get.Key.description
		}

		val guild by optionalGuild {
			name = TagsTranslations.Arguments.Get.Server.name
			description = TagsTranslations.Arguments.Get.Server.description
		}
	}

	internal class FindArgs : Arguments() {
		val category by optionalString {
			name = TagsTranslations.Arguments.Get.Category.name
			description = TagsTranslations.Arguments.Get.Category.description
		}

		val key by optionalString {
			name = TagsTranslations.Arguments.Get.Key.name
			description = TagsTranslations.Arguments.Get.Key.description
		}

		val guild by optionalGuild {
			name = TagsTranslations.Arguments.Get.Server.name
			description = TagsTranslations.Arguments.Get.Server.description
		}
	}

	internal class SetArgs(tagsData: TagsData) : Arguments() {
		val key by string {
			name = TagsTranslations.Arguments.Set.Key.name
			description = TagsTranslations.Arguments.Set.Key.description
		}

		val category by string {
			name = TagsTranslations.Arguments.Set.Category.name
			description = TagsTranslations.Arguments.Set.Category.description

			autoComplete {
				val categories = tagsData.getAllCategories(data.guildId.value)

				suggestStringMap(
					categories.associateWith { it },
					FilterStrategy.Contains
				)
			}
		}

		val colour by optionalColor {
			name = TagsTranslations.Arguments.Set.Colour.name
			description = TagsTranslations.Arguments.Set.Colour.description
		}

		val guild by optionalGuild {
			name = TagsTranslations.Arguments.Set.Server.name
			description = TagsTranslations.Arguments.Set.Server.description
		}
	}

	internal class EditArgs(tagsData: TagsData) : Arguments() {
		val key by string {
			name = TagsTranslations.Arguments.Edit.Key.name
			description = TagsTranslations.Arguments.Edit.Key.description
		}

		val guild by optionalGuild {
			name = TagsTranslations.Arguments.Edit.Server.name
			description = TagsTranslations.Arguments.Edit.Server.description
		}

		val category by optionalString {
			name = TagsTranslations.Arguments.Set.Category.name
			description = TagsTranslations.Arguments.Set.Category.description

			autoComplete {
				val categories = tagsData.getAllCategories(data.guildId.value)

				suggestStringMap(
					categories.associateWith { it },
					FilterStrategy.Contains
				)
			}
		}

		val colour by optionalColor {
			name = TagsTranslations.Arguments.Set.Colour.name
			description = TagsTranslations.Arguments.Set.Colour.description
		}
	}

	internal class ByCategoryArgs(tagsData: TagsData) : Arguments() {
		val category by string {
			name = TagsTranslations.Arguments.Get.Key.name
			description = TagsTranslations.Arguments.Get.Key.description

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
		override var title: Key = when {
			!isEditing && key != null ->
				TagsTranslations.Modal.Title.createWithTag
					.withNamedPlaceholders("tag" to key)

			!isEditing && key == null ->
				TagsTranslations.Modal.Title.create

			isEditing && key != null ->
				TagsTranslations.Modal.Title.editWithTag
					.withNamedPlaceholders("tag" to key)

			isEditing && key == null ->
				TagsTranslations.Modal.Title.edit

			else -> error("Should be unreachable!")
		}

		val tagTitle = lineText {
			label = TagsTranslations.Modal.Input.title

			initialValue = initialTagTitle?.toKey()
			translateInitialValue = false
		}

		val description = paragraphText {
			label = TagsTranslations.Modal.Input.content

			initialValue = initialDescription?.toKey()
			translateInitialValue = false
		}

		val imageUrl = lineText {
			label = TagsTranslations.Modal.Input.imageUrl

			initialValue = initialImageUrl?.toKey()

			translateInitialValue = false
			required = false
		}
	}

	internal class ByKeyArgs : Arguments() {
		val key by string {
			name = TagsTranslations.Arguments.Partial.Key.name
			description = TagsTranslations.Arguments.Partial.Key.description
		}
	}

	internal class ByTitleArgs : Arguments() {
		val title by string {
			name = TagsTranslations.Arguments.Partial.Title.name
			description = TagsTranslations.Arguments.Partial.Title.description
		}
	}

	internal class GetTagArgs(tagsData: TagsData) : Arguments() {
		val tagKey by string {
			name = TagsTranslations.Arguments.Get.Tag.name
			description = TagsTranslations.Arguments.Get.Tag.description

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
					potentialTags = potentialTags.filter { it.category.startsWith(category, true) }
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
			name = TagsTranslations.Arguments.User.name
			description = TagsTranslations.Arguments.User.description
		}
	}

	// endregion
}
