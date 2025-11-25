plugins {
    java
    id("org.springframework.boot") version "3.5.7" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
    id("org.openjfx.javafxplugin") version "0.1.0" apply false
}

group = "edu.semitotal"
version = "0.0.1-SNAPSHOT"

subprojects {
    apply(plugin = "java")

    repositories {
        mavenCentral()
    }

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(25)
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}
