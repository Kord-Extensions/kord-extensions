plugins {
    `kordex-module`
    `published-module`
    `dokka-module`
    `ksp-module`

    kotlin("plugin.serialization")
}

metadata {
    name = "KordEx: Java Time"
    description = "KordEx module that provides converters that support Java Time"
}

dependencies {
    implementation(libs.kotlin.stdlib)

    implementation(project(":kord-extensions"))
    implementation(project(":annotations"))

    ksp(project(":annotation-processor"))

    detektPlugins(libs.detekt)
    detektPlugins(libs.detekt.libraries)

    testImplementation(libs.groovy)  // For logback config
    testImplementation(libs.jansi)
    testImplementation(libs.junit)
    testImplementation(libs.logback)
}

dokkaModule {
    moduleName.set("Kord Extensions: Java Time")
}
