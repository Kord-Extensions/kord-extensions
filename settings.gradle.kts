rootProject.name = "kord-extensions"

include("annotations")
include("annotation-processor")

include("data-adapters:adapter-mongodb")

include("kord-extensions")

include("extra-modules:extra-mappings")
include("extra-modules:extra-mappings")
include("extra-modules:extra-phishing")
include("extra-modules:extra-pluralkit")
include("extra-modules:extra-tags")
include("extra-modules:extra-welcome")

include("modules:java-time")
include("modules:time4j")
include("modules:unsafe")

include("plugins")
include("plugins:plugin-load-test")
include("plugins:test-plugin-1")
include("plugins:test-plugin-2")
include("plugins:test-plugin-core")

include("test-bot")
include("token-parser")

include("web:backend")
include("web:frontend")
