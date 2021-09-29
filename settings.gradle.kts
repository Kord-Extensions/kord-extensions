// NOTE: UPDATE THIS IF YOU UPDATE THE LIBS.VERSIONS.TOML
// NOTE: All the plugins and plugin repositories moved to buildSrc/build.gradle.kts

rootProject.name = "kord-extensions"

enableFeaturePreview("VERSION_CATALOGS")

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("libs.versions.toml"))
        }
    }
}

include("annotations")
include("annotation-processor")

include("kord-extensions")

include("extra-modules:extra-common")
include("extra-modules:extra-mappings")

include("modules:java-time")
include("modules:time4j")
include("modules:unsafe")

include("token-parser")
