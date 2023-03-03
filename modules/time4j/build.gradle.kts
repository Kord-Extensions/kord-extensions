plugins {
    `kordex-module`
    `published-module`
    `dokka-module`
    `ksp-module`
}

metadata {
    name = "KordEx: Time4J"
    description = "KordEx module that provides converters that support Time4J"
}

dependencies {
    api(libs.time4j.base)
    api(libs.time4j.tzdata)

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
    moduleName.set("Kord Extensions: Time4J")
}
