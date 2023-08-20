plugins {
    `kordex-module`
    `published-module`
    `dokka-module`
}

metadata {
    name = "KordEx Adapters: MongoDB"
    description = "KordEx data adapter for MongoDB, including extra codecs"
}

repositories {
    maven {
        name = "Sonatype Snapshots"
        url = uri("https://oss.sonatype.org/content/repositories/snapshots")
    }
}

dependencies {
    detektPlugins(libs.detekt)
    detektPlugins(libs.detekt.libraries)

    implementation(libs.kotlin.stdlib)
    implementation(libs.kx.coro)
    implementation(libs.logging)
    implementation(libs.mongodb)

    implementation(project(":kord-extensions"))
}

group = "com.kotlindiscord.kord.extensions"
