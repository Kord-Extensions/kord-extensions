/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.storage

import dev.kordex.core.utils.envOrNull
import java.nio.file.Path
import kotlin.io.path.Path

/**
 * For data adapters storing files, this is where those files should be placed. Default to the current working
 * directory, but you can change this by providing the `STORAGE_FILE_ROOT` environment variable.
 */
public val storageFileRoot: Path = Path(
	envOrNull("STORAGE_FILE_ROOT") ?: "."
)
