import java.io.ByteArrayOutputStream
import java.net.URL

buildscript {
    repositories {
        maven {
            name = "KotDis"
            url = uri("https://maven.kotlindiscord.com/repository/maven-public/")
        }
    }
}

plugins {
    `maven-publish`

    kotlin("jvm")

    id("io.gitlab.arturbosch.detekt")
    id("org.jetbrains.dokka")
}

dependencies {
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.15.0")

//    api("com.kotlindiscord.pluralkot:PluralKot:1.0.0")

    api("dev.kord:kord-core:0.7.0-RC3")

    api("org.jetbrains.kotlinx:kotlinx-serialization-core:1.1.0")

    api("io.insert-koin:koin-core:3.0.1")
    api("io.insert-koin:koin-logger-slf4j:3.0.1")

    api("io.github.microutils:kotlin-logging:2.0.6") // Basic logging setup
    api("io.sentry:sentry:4.3.0")  // Needs to be transitive or bots will start breaking
    api("com.ibm.icu:icu4j:69.1")  // For translations

    implementation("org.apache.commons:commons-text:1.9")
    implementation("commons-validator:commons-validator:1.7")

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    testImplementation("io.insert-koin:koin-test:3.0.1")

    testImplementation("org.junit.jupiter:junit-jupiter:5.6.2")
    testImplementation("ch.qos.logback:logback-classic:1.2.3")
    testImplementation("org.codehaus.groovy:groovy:3.0.4")  // For logback config
}

val sourceJar = task("sourceJar", Jar::class) {
    dependsOn(tasks["classes"])
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

val javadocJar = task("javadocJar",Jar::class) {
    dependsOn("dokkaJavadoc")
    archiveClassifier.set("javadoc")
    from(tasks.javadoc)
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_9
    targetCompatibility = JavaVersion.VERSION_1_9
}

kotlin {
    explicitApi()
}

detekt {
    buildUponDefaultConfig = true
    config = files("../detekt.yml")

    autoCorrect = true
}

publishing {
    repositories {
        maven {
            name = "KotDis"

            url = if (project.version.toString().contains("SNAPSHOT")) {
                uri("https://maven.kotlindiscord.com/repository/maven-snapshots/")
            } else {
                uri("https://maven.kotlindiscord.com/repository/maven-releases/")
            }

            credentials {
                username = project.findProperty("kotdis.user") as String?
                    ?: System.getenv("KOTLIN_DISCORD_USER")

                password = project.findProperty("kotdis.password") as String?
                    ?: System.getenv("KOTLIN_DISCORD_PASSWORD")
            }

            version = project.version
        }
    }

    publications {
        create<MavenPublication>("maven") {
            from(components.getByName("java"))

            artifact(sourceJar)
            artifact(javadocJar)
        }
    }
}

fun runCommand(command: String): String {
    val output = ByteArrayOutputStream()

    project.exec {
        commandLine(command.split(" "))
        standardOutput = output
    }

    return output.toString().trim()
}

fun getCurrentGitBranch(): String {  // https://gist.github.com/lordcodes/15b2a4aecbeff7c3238a70bfd20f0931
    var gitBranch = "Unknown branch"

    try {
        gitBranch = runCommand("git rev-parse --abbrev-ref HEAD")
    } catch (t: Throwable) {
        println(t)
    }

    return gitBranch
}

tasks.dokkaHtml.configure {
    moduleName.set("Kord Extensions")

    dokkaSourceSets {
        configureEach {
            includeNonPublic.set(false)
            skipDeprecated.set(false)

            displayName.set("Kord Extensions")
            includes.from("packages.md")
            jdkVersion.set(8)

            sourceLink {
                localDirectory.set(file("${project.projectDir}/src/main/kotlin"))

                remoteUrl.set(
                    URL(
                        "https://github.com/Kotlin-Discord/kord-extensions/" +
                            "tree/${getCurrentGitBranch()}/kord-extensions/src/main/kotlin"
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

tasks.test {
    useJUnitPlatform()

    testLogging.showStandardStreams = true

    testLogging {
        events("PASSED", "FAILED", "SKIPPED", "STANDARD_OUT", "STANDARD_ERROR")
    }

    systemProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug")
}

tasks.build {
    this.finalizedBy(sourceJar, javadocJar)
}
