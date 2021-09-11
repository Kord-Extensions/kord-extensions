import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `maven-publish`
    signing

    id("io.gitlab.arturbosch.detekt")

    kotlin("jvm")
}

repositories {
    maven {
        name = "KotDis"
        url = uri("https://maven.kotlindiscord.com/repository/maven-public/")
    }

    maven {
        name = "FabricMC"
        url = uri("https://maven.fabricmc.net/")
    }

    maven {
        name = "QuiltMC (Releases)"
        url = uri("https://maven.quiltmc.org/repository/release/")
    }

    maven {
        name = "QuiltMC (Snapshots)"
        url = uri("https://maven.quiltmc.org/repository/snapshot/")
    }

    maven {
        name = "Shedaniel"
        url = uri("https://maven.shedaniel.me")
    }

    maven {
        name = "JitPack"
        url = uri("https://jitpack.io")
    }
}

dependencies {
    api(libs.linkie)

    detektPlugins(libs.detekt)

    implementation(libs.konf.core)
    implementation(libs.konf.toml)
    implementation(libs.logging)
    implementation(libs.kotlin.stdlib)

    testImplementation(libs.groovy)  // For logback config
    testImplementation(libs.logback)

    implementation(project(":kord-extensions"))
}

group = "com.kotlindiscord.kord.extensions"

val sourceJar = task("sourceJar", Jar::class) {
    dependsOn(tasks["classes"])
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

detekt {
    buildUponDefaultConfig = true
    config = rootProject.files("detekt.yml")

    autoCorrect = true
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_9
    targetCompatibility = JavaVersion.VERSION_1_9
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "9"
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

tasks.build {
    this.finalizedBy(sourceJar)
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
        }
    }
}

signing {
    useGpgCmd()
    sign(publishing.publications["maven"])
}
