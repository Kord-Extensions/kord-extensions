plugins {
    `kordex-module`
    `published-module`
    `dokka-module`
    `disable-explicit-api-mode`

    kotlin("plugin.serialization")
}

repositories {
    maven {
        name = "KotDis"
        url = uri("https://maven.kotlindiscord.com/repository/maven-public/")
    }
}

dependencies {
    detektPlugins(libs.detekt)

    implementation(libs.konf.core)
    implementation(libs.konf.toml)
    implementation(libs.logging)
    implementation(libs.kotlin.stdlib)

    implementation(project(":kord-extensions"))
}

kordex {
    jvmTarget.set("9")
    javaVersion.set(JavaVersion.VERSION_1_9)
}
