import java.net.URL

plugins {
    id("org.jetbrains.dokka")
}

val dokkaModuleExtensionName = "dokkaModule"

abstract class DokkaModuleExtension {
    abstract val moduleName: Property<String>
    abstract val includes: ListProperty<String>
}

extensions.create<DokkaModuleExtension>(dokkaModuleExtensionName)

tasks {
    afterEvaluate {
        val projectDir = project.projectDir.relativeTo(rootProject.rootDir).toString()
        dokkaHtml {
            val extension = project.extensions.getByName<DokkaModuleExtension>(dokkaModuleExtensionName)
            extension.moduleName.orNull?.let {
                moduleName.set(it)
            }

            dokkaSourceSets {
                configureEach {
                    includeNonPublic.set(false)
                    skipDeprecated.set(false)
                    extension.moduleName.orNull?.let {
                        displayName.set(it)
                    }
                    extension.includes.orNull?.let {
                        includes.from(*it.toTypedArray())
                    }
                    jdkVersion.set(8)

                    sourceLink {
                        localDirectory.set(file("${project.projectDir}/src/main/kotlin"))

                        remoteUrl.set(
                            URL(
                                "https://github.com/Kotlin-Discord/kord-extensions/" +
                                    "tree/${getCurrentGitBranch()}/${projectDir}/src/main/kotlin"
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
