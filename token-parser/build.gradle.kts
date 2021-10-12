plugins {
    `kordex-module`
    `published-module`
    `dokka-module`
    `tested-module`
}

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.logging) // Basic logging setup

    detektPlugins(libs.detekt)

    testApi(libs.kord)
    testImplementation(libs.groovy)  // For logback config
    testImplementation(libs.junit)
    testImplementation(libs.koin.test)
    testImplementation(libs.logback)
}

dokkaModule {
    moduleName.set("Kord Extensions: Token Parser")
}
