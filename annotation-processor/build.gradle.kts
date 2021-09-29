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
}

dokkaModule {
    moduleName.set("Kord Extensions: Annotation Processor")
}

kordex {
    jvmTarget.set("1.8")
}
