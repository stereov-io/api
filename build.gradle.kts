import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.0.21"
    kotlin("plugin.spring") version "2.0.21"
    kotlin("plugin.serialization") version "2.0.21"
    id("org.springframework.boot") version "3.4.1"
    id("io.spring.dependency-management") version "1.1.4"
}

group = "io.stereov.www"
version = project.property("version") as String

val accessToken: String? = properties["maven.accessToken"] as String? ?: System.getenv("GITEA_TOKEN")

repositories {
    mavenCentral()
}

val kotlinVersion = "2.0.21"
val kotlinxVersion = "1.10.1"
val springBootVersion = "3.4.1"
val log4jVersion = "2.24.3"
val testContainersVersion = "1.19.0"
val bucket4jVersion = "8.14.0"

dependencies {
    implementation("io.stereov.web:baseline:1.0.4")

    // Tests
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
        exclude(group = "org.mockito")
    }
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("io.mockk:mockk:1.13.13")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$kotlinxVersion")
    testImplementation("org.testcontainers:testcontainers:$testContainersVersion")
    testImplementation("org.testcontainers:junit-jupiter:$testContainersVersion")
    testImplementation("org.testcontainers:mongodb:$testContainersVersion")
}

configurations.all {
    exclude(group = "commons-logging", module = "commons-logging")
    exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
    exclude(group = "org.springframework.boot", module = "spring-boot-starter-tomcat")
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.add("-Xjsr305=strict")
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

tasks.withType<JavaExec> {
    jvmArgs = listOf("-DLog4jContextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector")
}


tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}
