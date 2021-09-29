import java.net.URL

plugins {
    id("org.jetbrains.dokka")
}

val dokkaModuleExtensionName = "dokkaModule"

abstract class DokkaModuleExtension {
    abstract val moduleName: Property<String>
}

extensions.create<DokkaModuleExtension>(dokkaModuleExtensionName)

tasks {
    afterEvaluate {
        dokkaHtml {
            val extension = extensions.getByName<DokkaModuleExtension>(dokkaModuleExtensionName)
            moduleName.set(extension.moduleName.get())

            dokkaSourceSets {
                configureEach {
                    includeNonPublic.set(false)
                    skipDeprecated.set(false)

                    displayName.set(extension.moduleName.get())
                    includes.from("packages.md")
                    jdkVersion.set(8)

                    sourceLink {
                        localDirectory.set(file("${project.projectDir}/src/main/kotlin"))

                        remoteUrl.set(
                            URL(
                                "https://github.com/Kotlin-Discord/kord-extensions/" +
                                    "tree/${getCurrentGitBranch()}/annotation-processor/src/main/kotlin"
                            )
                        )

                        remoteLineSuffix.set("#L")
                    }

                    externalDocumentationLink {
                        url.set(URL("http://kordlib.github.io/kord/common/common/"))
                    }

                    externalDocumentationLink {
                        url.set(URL("http://kordlib.github.io/kord/core/core/"))
                    }
                }
            }
        }
    }
}
