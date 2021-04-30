import java.io.ByteArrayOutputStream
import java.net.URL
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

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

    kotlin("jvm") version "1.4.31"

    id("com.github.jakemarsden.git-hooks") version "0.0.1"
    id("io.gitlab.arturbosch.detekt") version "1.15.0"
    id("org.jetbrains.dokka") version "1.4.10.2"
}

group = "com.kotlindiscord.kord.extensions"
version = rootProject.version

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "9"

    kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
    kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.contracts.ExperimentalContracts"

    kotlinOptions.useIR = true
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_9
    targetCompatibility = JavaVersion.VERSION_1_9
}

kotlin {
    explicitApi()
}

repositories {
    maven {
        name = "KotDis"
        url = uri("https://maven.kotlindiscord.com/repository/maven-public/")
    }
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

val printVersion = task("printVersion") {
    print(version.toString())
}

dependencies {
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.15.0")

//    api("com.kotlindiscord.pluralkot:PluralKot:1.0.0")

    api("dev.kord:kord-core:0.7.0-RC3")

    api("net.time4j:time4j-base:5.8")
    api("net.time4j:time4j-tzdata:5.0-2021a")

    api("org.jetbrains.kotlinx:kotlinx-serialization-core:1.1.0")

    api("io.insert-koin:koin-core:3.0.1-beta-2")
    api("io.insert-koin:koin-logger-slf4j:3.0.1-beta-2")

    api("io.sentry:sentry:4.3.0")  // Needs to be transitive or bots will start breaking

    implementation("com.ibm.icu:icu4j:69.1")  // For translations

    implementation("io.github.microutils:kotlin-logging:2.0.6")
    implementation("org.apache.commons:commons-text:1.9")
    implementation("commons-validator:commons-validator:1.7")

    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    testImplementation("io.insert-koin:koin-test:3.0.1-beta-2")

    testImplementation("org.junit.jupiter:junit-jupiter:5.6.2")
    testImplementation("ch.qos.logback:logback-classic:1.2.3")
    testImplementation("org.codehaus.groovy:groovy:3.0.4")  // For logback config
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

detekt {
    buildUponDefaultConfig = true
    config = files("detekt.yml")

    autoCorrect = true
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

gitHooks {
    setHooks(
        mapOf("pre-commit" to "kord-extensions:detekt")
    )
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
                username = project.findProperty("kotdis.user") as String? ?: System.getenv("KOTLIN_DISCORD_USER")
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
