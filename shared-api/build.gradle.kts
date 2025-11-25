plugins {
    java
}

val lombokVersion = "1.18.42"
val jacksonVersion = "2.19.2"

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")

    compileOnly("org.projectlombok:lombok:$lombokVersion")
    annotationProcessor("org.projectlombok:lombok:$lombokVersion")
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}
