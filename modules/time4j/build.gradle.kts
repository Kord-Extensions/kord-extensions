plugins {
    `kordex-module`
    `published-module`
    `dokka-module`
    `ksp-module`
}

dependencies {
    api(libs.time4j.base)
    api(libs.time4j.tzdata)

    implementation(libs.kotlin.stdlib)

    implementation(project(":kord-extensions"))
    implementation(project(":annotations"))

    ksp(project(":annotation-processor"))

    detektPlugins(libs.detekt)

    testImplementation(libs.groovy)  // For logback config
    testImplementation(libs.jansi)
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
