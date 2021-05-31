pluginManagement {
    repositories {
        gradlePluginPortal()
        jcenter()
    }
}

rootProject.name = "kord-extensions"

include("docs")
include("kord-extensions")

include("extra-modules:extra-common")
include("extra-modules:extra-mappings")

include("modules:java-time")
include("modules:time4j")
