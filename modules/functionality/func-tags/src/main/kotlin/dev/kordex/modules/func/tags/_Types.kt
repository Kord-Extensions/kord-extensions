/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

@file:Suppress("Filename")

package dev.kordex.modules.func.tags

import dev.kord.rest.builder.message.create.MessageCreateBuilder
import dev.kordex.modules.func.tags.data.Tag

/** Type alias representing a tag formatter callback. **/
typealias TagFormatter = suspend MessageCreateBuilder.(tag: Tag) -> Unit
