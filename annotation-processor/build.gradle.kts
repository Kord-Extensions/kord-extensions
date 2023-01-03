plugins {
    `kordex-module`
    `published-module`
    `dokka-module`
}

dependencies {
    implementation(libs.kotlin.stdlib)

    implementation(libs.koin.core)
    implementation(libs.kotlinpoet)
    implementation(libs.ksp)

    implementation(project(":annotations"))

    detektPlugins(libs.detekt)
    detektPlugins(libs.detekt.libraries)
}

dokkaModule {
    moduleName.set("Kord Extensions: Annotation Processor")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of("11"))
    }
}
