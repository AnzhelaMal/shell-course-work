plugins {
    java
    application
    id("org.openjfx.javafxplugin")
    id("org.beryx.jlink") version "3.1.2"
}

val javafxVersion = "25.0.1"

javafx {
    version = javafxVersion
    modules("javafx.controls", "javafx.fxml", "javafx.graphics")
}

val lombokVersion = "1.18.42"
val jacksonVersion = "2.19.2"

dependencies {
    implementation(project(":shared-api"))

    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
    implementation("org.apache.httpcomponents.client5:httpclient5:5.4.1")

    implementation("org.openjfx:javafx-controls:$javafxVersion")
    implementation("org.openjfx:javafx-fxml:$javafxVersion")
    implementation("org.openjfx:javafx-graphics:$javafxVersion")

    compileOnly("org.projectlombok:lombok:$lombokVersion")
    annotationProcessor("org.projectlombok:lombok:$lombokVersion")

    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

application {
    mainClass.set("edu.semitotal.commander.client.CommanderClient")
}

jlink {
    options.set(listOf("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages"))
    launcher {
        name = "TotalCommander"
    }
    jpackage {
        if (file("src/main/resources/icon.ico").exists()) {
            imageOptions.addAll(listOf("--icon", "src/main/resources/icon.ico"))
        }
        installerOptions.addAll(
            listOf(
                "--win-dir-chooser",
                "--win-shortcut",
                "--win-menu"
            )
        )
        installerType = "exe"
        installerName = "TotalCommander"
        appVersion = project.version.toString().replace("-SNAPSHOT", "")
    }
}

tasks.register("buildExe") {
    group = "build"
    description = "Build Windows .exe installer"
    dependsOn("jpackage")
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}
