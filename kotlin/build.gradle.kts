import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "2.4.4"
	id("io.spring.dependency-management") version "1.0.11.RELEASE"
	kotlin("jvm") version "1.4.31"
	kotlin("plugin.spring") version "1.4.31"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-security:2.4.4")
	implementation("org.springframework.boot:spring-boot-starter-web:2.4.4")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.11.4")
	implementation("org.jetbrains.kotlin:kotlin-reflect:1.4.31")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.4.31")

	testImplementation("org.springframework.boot:spring-boot-starter-test:2.4.4")
	testImplementation("org.springframework.security:spring-security-test:5.4.5")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "11"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
