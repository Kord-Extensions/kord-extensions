/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.commands.application.slash.converters

import com.kotlindiscord.kord.extensions.commands.application.slash.converters.impl.EnumChoiceConverter

/** Interface representing an enum used in the [EnumChoiceConverter]. **/
public interface ChoiceEnum {
	/** Human-readable name to show on Discord. **/
	public val readableName: String
}
