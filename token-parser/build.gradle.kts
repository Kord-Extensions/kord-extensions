plugins {
    `kordex-module`
    `published-module`
    `dokka-module`
    `tested-module`
}

metadata {
    name = "KordEx: Token Parser"
    description = "Simple token-based command parser, for parsing basic commands from messages sent on chat networks"
}

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.logging) // Basic logging setup

    detektPlugins(libs.detekt)
    detektPlugins(libs.detekt.libraries)

    testApi(libs.kord)
    testImplementation(libs.groovy)  // For logback config
    testImplementation(libs.jansi)
    testImplementation(libs.junit)
    testImplementation(libs.koin.test)
    testImplementation(libs.logback)
}

dokkaModule {
    moduleName.set("Kord Extensions: Token Parser")
}
