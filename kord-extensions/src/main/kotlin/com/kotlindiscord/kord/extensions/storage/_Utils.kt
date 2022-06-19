/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.storage

import com.kotlindiscord.kord.extensions.utils.envOrNull
import java.nio.file.Path
import kotlin.io.path.Path

/**
 * For data adapters storing files, this is where those files should be placed. Default to the current working
 * directory, but you can change this by providing the `STORAGE_FILE_ROOT` environment variable.
 */
public val storageFileRoot: Path = Path(
    envOrNull("STORAGE_FILE_ROOT") ?: "."
)
