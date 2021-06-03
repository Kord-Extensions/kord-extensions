pluginManagement {
    repositories {
        gradlePluginPortal()
        jcenter()

        plugins {
            // NOTE: UPDATE THIS IF YOU UPDATE THE LIBS.VERSIONS.TOML

            kotlin("jvm") version "1.5.10"
            kotlin("plugin.serialization") version "1.5.10"

            id("io.gitlab.arturbosch.detekt") version "1.17.1"
            id("org.jetbrains.dokka") version "1.4.10.2"
        }
    }
}

rootProject.name = "kord-extensions"

enableFeaturePreview("VERSION_CATALOGS")

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("libs.versions.toml"))
        }
    }
}

include("docs")
include("kord-extensions")

include("extra-modules:extra-common")
include("extra-modules:extra-mappings")

include("modules:java-time")
include("modules:time4j")
