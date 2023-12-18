import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.21"
    kotlin("plugin.serialization") version "1.9.21"
    application
}

group = "iem.fraunhofer.de"
version = "1.0-SNAPSHOT"

repositories {
    google()
    mavenCentral()

    exclusiveContent {
        forRepository {
            maven("https://androidx.dev/storage/compose-compiler/repository")
        }

        filter {
            includeGroup("androidx.compose.compiler")
        }
    }

    exclusiveContent {
        forRepository {
            maven("https://repo.gradle.org/gradle/libs-releases/")
        }

        filter {
            includeGroup("org.gradle")
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        apiVersion = "1.9"
        jvmTarget = "21"
    }
}

val exposedVersion = "0.45.0"
val ortVersion = "10.0.0"
val ktorVersion = "2.3.7"
val kotlinCoroutines = "1.7.3"

dependencies {
    implementation("com.github.ajalt.clikt:clikt:4.2.1")
    implementation("org.apache.logging.log4j:log4j-api-kotlin:1.3.0")
    implementation("org.apache.logging.log4j:log4j-to-slf4j:2.21.1")
    implementation("ch.qos.logback:logback-classic:1.4.11")
    implementation("io.github.z4kn4fein:semver:1.4.2")
    implementation("org.ossreviewtoolkit:analyzer:$ortVersion")
    implementation("org.ossreviewtoolkit:model:$ortVersion")
    implementation("org.ossreviewtoolkit:reporter:$ortVersion")
    implementation("org.ossreviewtoolkit.plugins.packagecurationproviders:package-curation-provider-api:$ortVersion")
    implementation("org.ossreviewtoolkit.plugins.packagemanagers:maven-package-manager:$ortVersion")
    implementation("org.ossreviewtoolkit.plugins.packagemanagers:gradle-package-manager:$ortVersion")
    implementation("org.ossreviewtoolkit.plugins.packagemanagers:nuget-package-manager:$ortVersion")
    implementation("org.ossreviewtoolkit.plugins.packagemanagers:cargo-package-manager:$ortVersion")
    implementation("org.ossreviewtoolkit.plugins.packagemanagers:node-package-manager:$ortVersion")
    implementation("org.ossreviewtoolkit.plugins.packagemanagers:go-package-manager:$ortVersion")
    implementation("org.ossreviewtoolkit.plugins.packagecurationproviders:ort-config-package-curation-provider:$ortVersion")
    implementation("org.ossreviewtoolkit.plugins.packagecurationproviders:clearly-defined-package-curation-provider:$ortVersion")
    implementation("org.ossreviewtoolkit.plugins.packagemanagers:python-package-manager:$ortVersion")
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-crypt:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-kotlin-datetime:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-json:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-money:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-spring-boot-starter:$exposedVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutines")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:$kotlinCoroutines")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:$kotlinCoroutines")
    implementation("io.ktor:ktor-client-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-client-cio-jvm:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    runtimeOnly("org.postgresql:postgresql:42.6.0")
    runtimeOnly("org.ossreviewtoolkit.plugins.packagecurationproviders:file-package-curation-provider:$ortVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.8.10")
    testImplementation("io.ktor:ktor-client-mock:$ktorVersion")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$kotlinCoroutines")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.16.0")
    testImplementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.16.0")
}

tasks.test {
    useJUnitPlatform()
}


application {
    mainClass.set("MainKt")
}