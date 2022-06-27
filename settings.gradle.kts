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

include("extra-modules:extra-mappings")
include("extra-modules:extra-mappings")
include("extra-modules:extra-phishing")
include("extra-modules:extra-pluralkit")

include("modules:java-time")
include("modules:time4j")
include("modules:unsafe")

include("test-bot")
include("token-parser")
