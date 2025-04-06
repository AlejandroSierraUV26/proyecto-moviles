plugins {
    kotlin("plugin.serialization") version "1.9.0" // o tu versión actual
    kotlin("jvm") version "1.9.22"
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core:2.3.7")
    implementation("io.ktor:ktor-server-netty:2.3.7")
    implementation("io.ktor:ktor-server-content-negotiation:2.3.7")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")

    implementation("ch.qos.logback:logback-classic:1.4.11")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
}


application {
    mainClass.set("com.backtor.ApplicationKt")
}
