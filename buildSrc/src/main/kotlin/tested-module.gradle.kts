plugins {
	java
}

tasks {
	test {
		useJUnitPlatform()

		testLogging.showStandardStreams = true

		testLogging {
			events("PASSED", "FAILED", "SKIPPED", "STANDARD_OUT", "STANDARD_ERROR")
		}

		systemProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug")
	}
}
