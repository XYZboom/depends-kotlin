import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
    kotlin("jvm") version "1.9.0"
    id("maven-publish")
    application
}

group = "com.github.xyzboom"
version = "1.0-SNAPSHOT"

repositories {
    mavenLocal()
    maven {
        url = uri("http://47.115.213.131:8080/repository/alex-release/")
        isAllowInsecureProtocol = true
    }
    mavenCentral()
}

publishing {
    repositories {
        maven {
            url = uri("http://47.115.213.131:8080/repository/alex-snapshots/")
            credentials {
                username = properties["maven-username"].toString()
                password = properties["maven-password"].toString()
            }
            isAllowInsecureProtocol = true
        }
    }
    publications {
        create<MavenPublication>("maven") {
            groupId = "cn.emergentdesign.se"
            artifactId = "depends-kotlin"
            version = "1.0.0-SNAPSHOT"

            from(components["java"])
        }
    }
}

dependencies {
    implementation("io.github.oshai:kotlin-logging-jvm:5.1.0")
    implementation("org.apache.logging.log4j:log4j-api:2.20.0")
    implementation("ch.qos.logback:logback-classic:1.4.7")
    implementation("cn.emergentdesign.se:depends-core:0.9.8-SNAPSHOT")
    implementation("cn.emergentdesign.se:depends-java:0.9.7") {
        exclude("cn.emergentdesign.se:depends-core:0.9.7")
    }
    implementation("org.antlr:antlr4:4.13.1")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
    testImplementation("cn.emergentdesign.se:utils:0.1.1")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("depends.Main")
}
