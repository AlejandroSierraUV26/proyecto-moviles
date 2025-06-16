plugins {
    kotlin("jvm") version "1.9.22"  // La versión de Kotlin puede variar
    kotlin("plugin.serialization") version "1.9.0"
    application  // Asegúrate de tener este plugin para que la tarea 'run' esté disponible
}

application {
    // La clase principal de tu aplicación
    mainClass.set("com.backtor.ApplicationKt")
}

repositories {
    mavenCentral()
    google()
    maven("https://repo.jetbrains.space/public/p/ktor/eap")
    maven("https://repo.jetbrains.space/public/p/exposed/maven")
}

dependencies {
    implementation("io.ktor:ktor-server-core:2.3.7")
    implementation("io.ktor:ktor-server-netty:2.3.7")
    implementation("io.ktor:ktor-server-content-negotiation:2.3.7")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("io.ktor:ktor-client-core:2.3.7")
    implementation("io.ktor:ktor-client-cio:2.3.7")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.7")


    // Exposed
    implementation("org.jetbrains.exposed:exposed-core:0.45.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.45.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.45.0")
    implementation("org.jetbrains.exposed:exposed-java-time:0.45.0")
    implementation("org.jetbrains.exposed:exposed-kotlin-datetime:0.45.0")


    implementation("ch.qos.logback:logback-classic:1.4.11")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.5.0")  // Asegúrate de tener la última versión compatible.
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
    implementation("org.postgresql:postgresql:42.7.2")
    implementation("com.zaxxer:HikariCP:5.0.1")

    implementation("com.sun.mail:jakarta.mail:2.0.1")


    testImplementation("io.ktor:ktor-server-tests:2.3.7")
    testImplementation("org.jetbrains.kotlin:kotlin-test")

    // Hashing
    implementation("org.mindrot:jbcrypt:0.4")
    implementation("joda-time:joda-time:2.12.5")

    implementation("com.auth0:java-jwt:4.4.0")

    implementation("io.ktor:ktor-server-auth:2.3.7")
    implementation("io.ktor:ktor-server-auth-jwt:2.3.7")

    //Cloudinary
    //implementation("com.cloudinary:kotlin-url-gen:1.7.0")
    implementation("com.cloudinary:cloudinary-http44:1.37.0")

    // Google API Client
    implementation("com.google.api-client:google-api-client:2.2.0")

    implementation("com.google.oauth-client:google-oauth-client-jetty:1.34.1")
    implementation("com.google.apis:google-api-services-oauth2:v2-rev157-1.25.0")



}

