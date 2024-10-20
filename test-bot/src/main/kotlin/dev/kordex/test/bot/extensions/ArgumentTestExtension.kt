/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

@file:Suppress("StringLiteralDuplication")

package dev.kordex.test.bot.extensions

import dev.kord.common.entity.ChannelType
import dev.kord.common.entity.ForumTag
import dev.kord.core.behavior.channel.asChannelOfOrNull
import dev.kord.core.entity.Attachment
import dev.kord.core.entity.Emoji
import dev.kord.core.entity.GuildEmoji
import dev.kord.core.entity.channel.Channel
import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.application.slash.converters.impl.stringChoice
import dev.kordex.core.commands.converters.impl.*
import dev.kordex.core.extensions.Extension
import dev.kordex.core.extensions.publicSlashCommand
import dev.kordex.core.i18n.toKey
import dev.kordex.core.utils.suggestStringCollection
import dev.kordex.core.utils.suggestStringMap

public class ArgumentTestExtension : Extension() {
	override val name: String = "kordex.test-args"

	override suspend fun setup() {
		publicSlashCommand(::TagArgs) {
			name = "test-tag".toKey()
			description = "Test the tags converter".toKey()

			action {
				respond {
					content = "Channel provided: `${arguments.channel?.mention}`\n" +
						"Tag provided: `${arguments.tag?.name}`"
				}
			}
		}

		publicSlashCommand(::EmojiArguments) {
			name = "test-emoji".toKey()
			description = "Test the emoji converter".toKey()

			action {
				respond {
					val type = if (arguments.emoji is GuildEmoji) {
						"Guild"
					} else {
						"Unicode"
					}

					content = "$type emoji provided: `${arguments.emoji.mention}` (`${arguments.emoji.name}`)"
				}
			}
		}

		publicSlashCommand(::OptionalArgs) {
			name = "optional-autocomplete".toKey()
			description = "Check whether autocomplete works with an optional converter.".toKey()

			action {
				respond {
					content = "You provided: `${arguments.response}`"
				}
			}
		}

		publicSlashCommand(::LengthConstrainedArgs) {
			name = "length-constrained".toKey()
			description = "Check if length limits work".toKey()

			action {
				respond {
					content = buildString {
						append("You name is: `${arguments.name}`")
						arguments.lastName?.let {
							append(" `$it`")
						}
					}
				}
			}
		}

		publicSlashCommand(::AttachmentArguments) {
			name = "attachment".toKey()
			description = "Check attachment command options.".toKey()

			action {
				respond {
					content = buildString {
						append("You attached: ${arguments.file.filename}.")

						arguments.optionalFile?.let {
							append("\nYou also attached: ${it.filename}")
						}
					}
				}
			}
		}

		publicSlashCommand(::ChannelArguments) {
			name = "channel".toKey()
			description = "Check channel command options.".toKey()

			action {
				respond {
					content = buildString {
						append("You specified: ${arguments.channel.mention}.")
					}
				}
			}
		}

		publicSlashCommand(::AutocompleteArguments) {
			name = "autocomplete".toKey()
			description = "Test auto-completion events".toKey()

			action {
				respond {
					content = buildString {
						appendLine("**One:** ${arguments.one}")
						appendLine("**Two:** ${arguments.two}")
					}
				}
			}
		}
	}

	public inner class TagArgs : Arguments() {
		override val parseForAutocomplete: Boolean = true

		public val channel: Channel? by optionalChannel {
			name = "channel".toKey()
			description = "Channel to select a tag from".toKey()

			requireChannelType(ChannelType.GuildForum)
		}

		public val tag: ForumTag? by optionalTag {
			name = "tag".toKey()
			description = "Tag to use".toKey()

			channelGetter = {
				channel?.asChannelOfOrNull()
			}
		}
	}

	public inner class OptionalArgs : Arguments() {
		public val response: String? by optionalString {
			name = "response".toKey()
			description = "Text to receive".toKey()

			autoComplete {
				suggestStringMap(
					mapOf(
						"one" to "One",
						"two" to "Two",
						"three" to "Three"
					)
				)
			}
		}
	}

	public inner class LengthConstrainedArgs : Arguments() {
		public val name: String by string {
			name = "name".toKey()
			description = "The user's name.".toKey()
			minLength = 3
			maxLength = 10
		}

		public val lastName: String? by optionalString {
			name = "last-name".toKey()
			description = "The user's last name.".toKey()
			minLength = 4
			maxLength = 15
		}
	}

	public inner class AttachmentArguments : Arguments() {
		public val file: Attachment by attachment {
			name = "file".toKey()
			description = "An attached file.".toKey()
		}

		public val optionalFile: Attachment? by optionalAttachment {
			name = "optional-file".toKey()
			description = "An optional file.".toKey()
		}
	}

	public inner class ChannelArguments : Arguments() {
		public val channel: Channel by channel {
			name = "channel".toKey()
			description = "A text channel".toKey()

			requireChannelType(ChannelType.GuildText)
		}
	}

	public inner class EmojiArguments : Arguments() {
		public val emoji: Emoji by emoji {
			name = "emoji".toKey()
			description = "A custom or Unicode emoji".toKey()
		}
	}

	public inner class AutocompleteArguments : Arguments() {
		override val parseForAutocomplete: Boolean = true

		public val one: String by stringChoice {
			name = "one".toKey()
			description = "Choice argument".toKey()

			choice("O".toKey(), "o")
			choice("T".toKey(), "t")
			choice("F".toKey(), "f")
		}

		public val two: String by string {
			name = "two".toKey()
			description = "Autocomplete argument".toKey()

			autoComplete {
				suggestStringCollection(
					listOf("one", "two", "three", "four")
						.filter { it.contains(one.lowercase()) }
				)
			}
		}
	}
}
