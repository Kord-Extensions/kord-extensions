plugins {
    `kordex-module`
    `published-module`
    `dokka-module`
}

metadata {
    name = "KordEx: Annotations"
    description = "Annotation definitions to be processed by the KordEx annotation processor"
}

dependencies {
    implementation(libs.kotlin.stdlib)

    detektPlugins(libs.detekt)
    detektPlugins(libs.detekt.libraries)
}

dokkaModule {
    moduleName.set("Kord Extensions: Annotation Processor")
}
