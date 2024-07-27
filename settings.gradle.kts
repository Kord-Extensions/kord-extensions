rootProject.name = "kord-extensions"

include("annotations:annotations")
include("annotations:annotation-processor")

include("kord-extensions")

include("modules:data:data-mongodb")
include("modules:dev:dev-java-time")
include("modules:dev:dev-time4j")
include("modules:dev:dev-unsafe")
include("modules:functionality:func-mappings")
include("modules:functionality:func-phishing")
include("modules:functionality:func-tags")
include("modules:functionality:func-welcome")
include("modules:integrations:pluralkit")
include("modules:web:web-core:web-backend")
include("modules:web:web-core:web-frontend")

include("test-bot")
include("token-parser")
