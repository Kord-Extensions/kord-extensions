plugins {
    `kordex-module`
    `published-module`
    `dokka-module`
    `disable-explicit-api-mode`
}

repositories {
    maven {
        name = "KotDis"
        url = uri("https://maven.kotlindiscord.com/repository/maven-public/")
    }
}

dependencies {
    detektPlugins(libs.detekt)

    implementation(libs.logging)
    implementation(libs.kotlin.stdlib)
    implementation(libs.ktor.logging)

    testImplementation(libs.groovy)  // For logback config
    testImplementation(libs.logback)

    implementation(project(":kord-extensions"))
}

group = "com.kotlindiscord.kord.extensions"

kordex {
    jvmTarget.set("9")
    javaVersion.set(JavaVersion.VERSION_1_9)
}
