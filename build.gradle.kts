import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.23"
    kotlin("plugin.serialization") version "1.9.23"
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


kotlin {
    jvmToolchain(21)
}


val exposedVersion = "0.47.0"
val ortVersion = "18.0.0"
val ktorVersion = "2.3.9"
val kotlinCoroutines = "1.8.0"
val logback = "1.5.3"
val log4j = "2.23.1"

dependencies {
    implementation("com.google.guava:guava:33.1.0-jre")
    implementation("com.github.ajalt.clikt:clikt:4.2.2")
    implementation("org.apache.logging.log4j:log4j-api:$log4j")
    implementation("org.apache.logging.log4j:log4j-api-kotlin:1.4.0")
    implementation("org.apache.logging.log4j:log4j-to-slf4j:$log4j")
    implementation("ch.qos.logback:logback-classic:$logback")
    implementation("io.github.z4kn4fein:semver:1.4.2")
    implementation("org.ossreviewtoolkit:analyzer:$ortVersion")
    implementation("org.ossreviewtoolkit:model:$ortVersion")
    implementation("org.ossreviewtoolkit:reporter:$ortVersion")
    implementation("org.ossreviewtoolkit.plugins.packagecurationproviders:package-curation-provider-api:$ortVersion")
    implementation("org.ossreviewtoolkit.plugins.packageconfigurationproviders:package-configuration-provider-api:$ortVersion")
    implementation("org.ossreviewtoolkit.plugins.packageconfigurationproviders:ort-config-package-configuration-provider:$ortVersion")
    implementation("org.ossreviewtoolkit.plugins.packagemanagers:maven-package-manager:$ortVersion")
    implementation("org.ossreviewtoolkit.plugins.packagemanagers:gradle-package-manager:$ortVersion")
    implementation("org.ossreviewtoolkit.plugins.packagemanagers:gradle-inspector:$ortVersion")
    implementation("org.ossreviewtoolkit.plugins.packagemanagers:nuget-package-manager:$ortVersion")
    implementation("org.ossreviewtoolkit.plugins.packagemanagers:cargo-package-manager:$ortVersion")
    implementation("org.ossreviewtoolkit.plugins.packagemanagers:node-package-manager:$ortVersion")
    implementation("org.ossreviewtoolkit.plugins.packagemanagers:go-package-manager:$ortVersion")
    implementation("org.ossreviewtoolkit.plugins.packagecurationproviders:ort-config-package-curation-provider:$ortVersion")
    implementation("org.ossreviewtoolkit.plugins.packagecurationproviders:clearly-defined-package-curation-provider:$ortVersion")
    implementation("org.ossreviewtoolkit.plugins.packagemanagers:python-package-manager:$ortVersion")
    implementation("org.ossreviewtoolkit.plugins.versioncontrolsystems:git-version-control-system:$ortVersion")
    implementation("org.eclipse.jgit:org.eclipse.jgit:6.9.0.202403050737-r")
    implementation("org.jetbrains.kotlinx:kandy-lets-plot:0.5.0")
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-crypt:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-kotlin-datetime:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-json:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-money:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-spring-boot-starter:$exposedVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutines")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:$kotlinCoroutines")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:$kotlinCoroutines")
    implementation("io.ktor:ktor-client-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-client-apache5:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-client-apache:2.3.9")
    runtimeOnly("org.postgresql:postgresql:42.7.3")
    runtimeOnly("org.xerial:sqlite-jdbc:3.45.2.0")
    runtimeOnly("org.ossreviewtoolkit.plugins.packagecurationproviders:file-package-curation-provider:$ortVersion")
    runtimeOnly("org.ossreviewtoolkit.utils:common-utils:$ortVersion")
    runtimeOnly("org.ossreviewtoolkit.utils:ort-utils:$ortVersion")
    runtimeOnly("org.ossreviewtoolkit.plugins.packagemanagers:gradle-model:$ortVersion")
    runtimeOnly("org.ossreviewtoolkit.plugins.packageconfigurationproviders:dir-package-configuration-provider:$ortVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.9.23")
    testImplementation("io.ktor:ktor-client-mock:$ktorVersion")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$kotlinCoroutines")
    testImplementation("io.mockk:mockk:1.13.10")
    testImplementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.0")
    testImplementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.17.0")
}

tasks.test {
    useJUnitPlatform()
}


application {
    mainClass.set("MainKt")
}