/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

@file:Suppress("Filename")

package com.kotlindiscord.kord.extensions.modules.extra.tags

import com.kotlindiscord.kord.extensions.modules.extra.tags.data.Tag
import dev.kord.rest.builder.message.create.MessageCreateBuilder

/** Type alias representing a tag formatter callback. **/
typealias TagFormatter = suspend MessageCreateBuilder.(tag: Tag) -> Unit
