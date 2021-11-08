plugins {
    java
    id("com.google.devtools.ksp")
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
