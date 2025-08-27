plugins {
    application
    kotlin("jvm") version "2.1.21"
}

group = "com.example.assignment"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("args4j:args4j:2.37")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

application {
    applicationName = "incjavac"
    mainClass = "com.example.assignment.MainKt"
}