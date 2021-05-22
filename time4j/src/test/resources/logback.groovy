import ch.qos.logback.core.joran.spi.ConsoleTarget

def environment = System.getenv().getOrDefault("ENVIRONMENT", "production")

def defaultLevel = DEBUG

if (environment == "spam") {
    logger("dev.kord.rest.DefaultGateway", TRACE)
} else {
    // Silence warning about missing native PRNG
    logger("io.ktor.util.random", ERROR)
}

appender("CONSOLE", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%d{yyyy-MM-dd HH:mm:ss:SSS Z} | %5level | %40.40logger{40} | %msg%n"
    }

    target = ConsoleTarget.SystemErr
}

appender("FILE", FileAppender) {
    file = "output.log"

    encoder(PatternLayoutEncoder) {
        pattern = "%d{yyyy-MM-dd HH:mm:ss:SSS Z} | %5level | %40.40logger{40} | %msg%n"
    }
}

root(defaultLevel, ["CONSOLE", "FILE"])
