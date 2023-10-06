import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")

	id("com.github.ben-manes.versions")
    id("io.gitlab.arturbosch.detekt")
    id("org.cadixdev.licenser")
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
    from(tasks.javadoc)
}

tasks {
    build {
        finalizedBy(sourceJar, javadocJar)
    }

    kotlin {
        explicitApi()
    }

    jar {
        from(rootProject.file("build/LICENSE-kordex"))
    }

    afterEvaluate {
        rootProject.file("LICENSE").copyTo(rootProject.file("build/LICENSE-kordex"), true)

        tasks.withType<JavaCompile>().configureEach {
            sourceCompatibility = "13"
            targetCompatibility = "13"
        }

        withType<KotlinCompile>().configureEach {
            compilerOptions {
                freeCompilerArgs.add("-Xallow-kotlin-package")
            }

            kotlinOptions {
                jvmTarget = "13"
            }
        }
    }
}

detekt {
    buildUponDefaultConfig = true
    config.from(files("$rootDir/detekt.yml"))

    autoCorrect = true
}

license {
    setHeader(rootProject.file("LICENSE"))
    ignoreFailures(System.getenv()["CI"] == null)

    include ("**/src/**.*")
    include ("src/**.*")
}
