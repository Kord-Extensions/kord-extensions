import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
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
    signing

    kotlin("jvm")

    id("com.google.devtools.ksp")
    id("io.gitlab.arturbosch.detekt")
    id("org.jetbrains.dokka")
}

dependencies {
    api(libs.icu4j)  // For translations
    api(libs.koin.core)
    api(libs.koin.logger)
    api(libs.kord)
    api(libs.logging) // Basic logging setup
    api(libs.kx.ser)
    api(libs.sentry)  // Needs to be transitive or bots will start breaking

    api(project(":token-parser"))

    detektPlugins(libs.detekt)

    implementation(libs.bundles.commons)
    implementation(libs.kotlin.stdlib)

    implementation(project(":annotations"))

    testImplementation(libs.groovy)  // For logback config
    testImplementation(libs.junit)
    testImplementation(libs.koin.test)
    testImplementation(libs.logback)

    ksp(project(":annotation-processor"))
}

val sourceJar = task("sourceJar", Jar::class) {
    dependsOn(tasks["classes"])
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

val javadocJar = task("javadocJar", Jar::class) {
    dependsOn("dokkaJavadoc")
    archiveClassifier.set("javadoc")
    from(tasks.javadoc)
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

kotlin {
    explicitApi()
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

sourceSets {
    main {
        java {
            srcDir(file("$buildDir/generated/ksp/main/kotlin/"))
        }
    }

    test {
        java {
            srcDir(file("$buildDir/generated/ksp/test/kotlin/"))
        }
    }
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

signing {
    useGpgCmd()
    sign(publishing.publications["maven"])
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

val compileKotlin: KotlinCompile by tasks

compileKotlin.kotlinOptions {
    languageVersion = "1.5"
}
