/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.joran.spi.ConsoleTarget
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.FileAppender

def environment = System.getenv("ENVIRONMENT") ?: "dev"
def defaultLevel = TRACE

if (environment == "spam") {
	logger("dev.kord.rest.DefaultGateway", TRACE)
} else {
	// Silence warning about missing native PRNG
	logger("io.ktor.util.random", ERROR)
}

appender("CONSOLE", ConsoleAppender) {
	encoder(PatternLayoutEncoder) {
		pattern = "%boldGreen(%d{yyyy-MM-dd}) %boldYellow(%d{HH:mm:ss}) %gray(|) %highlight(%5level) %gray(|) %boldMagenta(%40.40logger{40}) %gray(|) %msg%n"

		withJansi = true
	}

	target = ConsoleTarget.SystemOut
}

appender("FILE", FileAppender) {
	file = "output.log"

	encoder(PatternLayoutEncoder) {
		pattern = "%d{yyyy-MM-dd HH:mm:ss:SSS Z} | %5level | %40.40logger{40} | %msg%n"
	}
}

root(defaultLevel, ["CONSOLE", "FILE"])
