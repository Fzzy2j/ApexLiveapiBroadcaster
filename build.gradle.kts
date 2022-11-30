import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.github.johnrengelman.shadow") version "7.1.2"
    kotlin("jvm") version "1.7.20"
    application
}

group = "com.esportsarena.liveapibroadcast"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

tasks {
    withType<Jar> {
        manifest {
            attributes["Main-Class"] = "com.esportsarena.liveapibroadcast.MainKt"
        }
    }
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.json:json:20220924")
    implementation("com.google.code.gson:gson:2.10")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("MainKt")
}