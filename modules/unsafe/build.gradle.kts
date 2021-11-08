plugins {
    `kordex-module`
    `published-module`
    `dokka-module`
    `ksp-module`
}

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(project(":kord-extensions"))

    detektPlugins(libs.detekt)

    testImplementation(libs.groovy)  // For logback config
    testImplementation(libs.junit)
    testImplementation(libs.logback)

    ksp(project(":annotation-processor"))
}

kordex {
    jvmTarget.set("9")
    javaVersion.set(JavaVersion.VERSION_1_9)
}

dokkaModule {
    moduleName.set("Kord Extensions: Unsafe")
}
