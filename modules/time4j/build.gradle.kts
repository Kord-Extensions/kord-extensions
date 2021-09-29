plugins {
    `kordex-module`
    `published-module`
    `dokka-module`
}

dependencies {
    api(libs.time4j.base)
    api(libs.time4j.tzdata)

    implementation(libs.kotlin.stdlib)
    implementation(project(":kord-extensions"))

    detektPlugins(libs.detekt)

    testImplementation(libs.groovy)  // For logback config
    testImplementation(libs.junit)
    testImplementation(libs.logback)
}

kordex {
    jvmTarget.set("9")
    javaVersion.set(JavaVersion.VERSION_1_9)
}

dokkaModule {
    moduleName.set("Kord Extensions: Time4J")
}
