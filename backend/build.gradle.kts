plugins {
    kotlin("plugin.serialization") version "1.9.0"
    kotlin("jvm") version "1.9.22"
    application
}

repositories {
    mavenCentral()
    maven("https://repo.jetbrains.space/public/p/ktor/eap")
    maven("https://repo.jetbrains.space/public/p/exposed/maven")
}


dependencies {
    implementation("io.ktor:ktor-server-core:2.3.7")
    implementation("io.ktor:ktor-server-netty:2.3.7")
    implementation("io.ktor:ktor-server-content-negotiation:2.3.7")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")

    // Exposed
    implementation("org.jetbrains.exposed:exposed-core:0.45.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.45.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.45.0")
    implementation("org.jetbrains.exposed:exposed-java-time:0.45.0")
    implementation("org.jetbrains.exposed:exposed-kotlin-datetime:0.45.0")


    implementation("ch.qos.logback:logback-classic:1.4.11")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation("org.postgresql:postgresql:42.7.2") // Driver de PostgreSQL
    implementation("com.zaxxer:HikariCP:5.0.1")         // Pool de conexiones

    testImplementation("io.ktor:ktor-server-tests:2.3.7")
    testImplementation("org.jetbrains.kotlin:kotlin-test")

    // Hashing
    implementation("org.mindrot:jbcrypt:0.4")

    // Joda Time
    implementation("joda-time:joda-time:2.12.5")


}


application {
    mainClass.set("com.backtor.ApplicationKt")
}
