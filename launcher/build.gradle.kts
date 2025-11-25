plugins {
    java
    application
    id("org.springframework.boot")
    id("org.openjfx.javafxplugin")
    id("io.spring.dependency-management")
    id("org.beryx.runtime") version "2.0.0"
}

extra["springModulithVersion"] = "1.4.4"

val lombokVersion = "1.18.42"

dependencies {
    implementation(project(":server"))
    implementation(project(":client"))
    implementation(project(":shared-api"))

    implementation("org.apache.httpcomponents.client5:httpclient5:5.4.1")
    implementation("org.springframework.boot:spring-boot-starter")

    compileOnly("org.projectlombok:lombok:$lombokVersion")
    annotationProcessor("org.projectlombok:lombok:$lombokVersion")
}

application {
    mainClass.set("edu.semitotal.commander.launcher.CommanderLauncher")
}

runtime {
    options.set(listOf("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages"))

    addModules(
        "java.base",
        "java.compiler",
        "java.desktop",
        "java.instrument",
        "java.logging",
        "java.management",
        "java.management.rmi",
        "java.naming",
        "java.net.http",
        "java.prefs",
        "java.rmi",
        "java.scripting",
        "java.se",
        "java.security.jgss",
        "java.security.sasl",
        "java.sql",
        "java.sql.rowset",
        "java.transaction.xa",
        "java.xml",
        "java.xml.crypto",
        "jdk.unsupported",
        "jdk.crypto.ec",
        "jdk.naming.dns",
    )

    jpackage {
        val iconFile = file("${project.rootDir}/client/src/main/resources/icon.ico")
        if (iconFile.exists()) {
            imageOptions.addAll(listOf("--icon", iconFile.absolutePath))
        }

        // Copy docker-compose.yml to the app directory
        val composeFile = file("src/main/resources/docker-compose.yml")
        if (composeFile.exists()) {
            imageOptions.addAll(listOf(
                "--resource-dir", file("src/main/resources").absolutePath
            ))
        }

        installerOptions.addAll(
            listOf(
                "--win-dir-chooser",
                "--win-shortcut",
                "--win-menu",
                "--win-menu-group", "Total Commander"
            )
        )

        installerType = "exe"
        installerName = "TotalCommander"
        appVersion = project.version.toString().replace("-SNAPSHOT", "")
    }
}

tasks.named<ProcessResources>("processResources") {
    from("${project.rootDir}/compose.yml") {
        into(".")
    }
}

tasks.register("buildExe") {
    group = "build"
    description = "Build Windows .exe installer"
    dependsOn("jpackage")
}

tasks.register<Jar>("fatJar") {
    group = "build"
    description = "Assembles a fat jar including all dependencies."
    archiveClassifier.set("all")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    manifest {
        attributes["Main-Class"] = "edu.semitotal.commander.launcher.CommanderLauncher"
    }

    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    from(sourceSets.main.get().output)
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.modulith:spring-modulith-bom:${property("springModulithVersion")}")
    }
}