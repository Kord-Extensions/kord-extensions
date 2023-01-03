plugins {
    `kordex-module`
    `published-module`
    `dokka-module`
    `disable-explicit-api-mode`
    `ksp-module`
}

repositories {
    maven {
        name = "KotDis"
        url = uri("https://maven.kotlindiscord.com/repository/maven-public/")
    }

    maven {
        name = "FabricMC"
        url = uri("https://maven.fabricmc.net/")
    }

    maven {
        name = "QuiltMC (Releases)"
        url = uri("https://maven.quiltmc.org/repository/release/")
    }

    maven {
        name = "QuiltMC (Snapshots)"
        url = uri("https://maven.quiltmc.org/repository/snapshot/")
    }

    maven {
        name = "Shedaniel"
        url = uri("https://maven.shedaniel.me")
    }

    maven {
        name = "JitPack"
        url = uri("https://jitpack.io")
    }
}

dependencies {
    api(libs.linkie)

    detektPlugins(libs.detekt)
    detektPlugins(libs.detekt.libraries)

    implementation(libs.konf.core)
    implementation(libs.konf.toml)
    implementation(libs.logging)
    implementation(libs.kotlin.stdlib)

    testImplementation(libs.groovy)  // For logback config
    testImplementation(libs.jansi)
    testImplementation(libs.logback)

    implementation(project(":kord-extensions"))
    implementation(project(":annotations"))

    ksp(project(":annotation-processor"))
}

group = "com.kotlindiscord.kord.extensions"
