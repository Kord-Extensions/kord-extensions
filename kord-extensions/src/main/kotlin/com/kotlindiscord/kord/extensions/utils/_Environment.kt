@file:OptIn(ExperimentalPathApi::class)

package com.kotlindiscord.kord.extensions.utils

import mu.KotlinLogging
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.Path
import kotlin.io.path.isRegularFile
import kotlin.io.path.readLines
import kotlin.system.exitProcess

private var firstLoad: Boolean = true
private var logger = KotlinLogging.logger {}
private val envMap: MutableMap<String, String> = mutableMapOf()

/**
 * Returns the value of an environmental variable, loading from a `.env` file in the current working directory if
 * possible.
 *
 * This function caches the contents of the `.env` file the first time it's called - there's no way to parse the file
 * again later.
 *
 * @param name Environmental variable to get the value for.
 * @return The value of the environmental variable, or `null` if it doesn't exist.
 */
public fun env(name: String): String? {
    if (firstLoad) {
        firstLoad = false

        val dotenvFile = Path(".env")

        if (dotenvFile.isRegularFile()) {
            logger.info { "Loading environment variables from .env file" }

            val lines = dotenvFile.readLines()

            for (line in lines) {
                if (line.startsWith("#"))
                    continue

                var effectiveLine = line
                if (line.contains("#")) {
                    effectiveLine = effectiveLine.substring(0, line.indexOf("#"))
                }

                if (!effectiveLine.contains('=')) {
                    logger.warn {
                        "Invalid line in dotenv file: \"=\" not found\n" +
                            "    $effectiveLine"
                    }

                    continue
                }

                val split = effectiveLine.split("=", limit = 2)

                if (split.size != 2) {
                    logger.warn {
                        "Invalid line in dotenv file: variables must be of the form \"name=value\"\n" +
                            " -> $effectiveLine"
                    }

                    continue
                }

                logger.debug { "${split[0]} -> ${split[1]}" }

                envMap[split[0]] = split[1]
            }
        }
    }

    return envMap[name] ?: System.getenv()[name]
}
