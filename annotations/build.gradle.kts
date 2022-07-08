plugins {
    `kordex-module`
    `published-module`
    `dokka-module`
}

dependencies {
    implementation(libs.kotlin.stdlib)

    detektPlugins(libs.detekt)
}

dokkaModule {
    moduleName.set("Kord Extensions: Annotation Processor")
}
