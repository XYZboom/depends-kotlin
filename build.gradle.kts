import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
    kotlin("jvm") version "1.9.0"
    id("com.github.johnrengelman.shadow") version "7.0.0"
    application
    `maven-publish`
}

group = "com.github.XYZboom"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://jitpack.io")
}
publishing {
    repositories {
        mavenLocal()
    }
    publications {
        create<MavenPublication>("depend-core") {
            groupId = "com.github.XYZboom"
            artifactId = "depends-kotlin"
            version = "1.0.0-SNAPSHOT"

            from(components["java"])
        }
        create<MavenPublication>("depend-core-source") {
            groupId = "com.github.XYZboom"
            artifactId = "depends-kotlin"
            version = "1.0.0-SNAPSHOT"

            // 配置要上传的源码
            artifact(tasks.register<Jar>("sourcesJar") {
                from(sourceSets.main.get().allSource)
                archiveClassifier.set("sources")
            }) {
                classifier = "sources"
            }
        }
    }
}
dependencies {
    implementation("io.github.oshai:kotlin-logging-jvm:5.1.0")
    implementation("org.apache.logging.log4j:log4j-api:2.20.0")
    implementation("org.slf4j:slf4j-log4j12:2.0.10")
//    implementation("ch.qos.logback:logback-classic:1.4.7")
    implementation("com.github.XYZboom:depends-core:1.0.0-alpha6")
    implementation("com.github.XYZboom:depends-java:1.0.0-alpha3")
    implementation("org.antlr:antlr4:4.13.1")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
    testImplementation("com.github.multilang-depends:utils:04855aebf3")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

application {
    mainClass.set("depends.Main")
}

tasks.withType(ShadowJar::class.java) {
    manifest {
        attributes["Main-Class"] = "depends.Main"
    }
}