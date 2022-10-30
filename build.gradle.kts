import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.7.5"
    id("io.spring.dependency-management") version "1.0.15.RELEASE"
    kotlin("jvm") version "1.6.21"
    kotlin("plugin.spring") version "1.6.21"
}

group = "com.myservice"
version = "1.0.0-RELEASE"
java.sourceCompatibility = JavaVersion.VERSION_17

val springdocVersion = "1.6.12"
val okHttpVersion = "4.10.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")

    /* swagger */
    implementation("org.springdoc:springdoc-openapi-webflux-core:$springdocVersion")
    implementation("org.springdoc:springdoc-openapi-webflux-ui:$springdocVersion")
    implementation("org.springdoc:springdoc-openapi-kotlin:$springdocVersion")


    implementation("io.github.microutils:kotlin-logging-jvm:3.0.2")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("de.flapdoodle.embed:de.flapdoodle.embed.mongo:3.5.1")

    implementation("com.squareup.okhttp3:okhttp:$okHttpVersion")
    testImplementation("com.squareup.okhttp3:mockwebserver:$okHttpVersion")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
