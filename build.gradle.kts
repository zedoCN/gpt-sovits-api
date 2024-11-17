plugins {
    java
    kotlin("jvm") version "+"
    kotlin("plugin.serialization") version "1.8.0"
    `maven-publish`
}

group = "top.zedo"
version = "1.0"


tasks {
    register<Jar>("sourcesJar") {
        from(sourceSets["main"].allSource)
        archiveClassifier.set("sources")
    }
    register<Jar>("javadocJar") {
        from(file("build/docs/javadoc"))  // 使用 Javadoc 任务的输出作为 JAR 文件内容
        archiveClassifier.set("javadoc")
    }
}


publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            pom {
                name.set("GPT-SoVITS API")
                description.set("API for SoVITS")
                url.set("https://zedo.top:408")

                developers {
                    developer {
                        id.set("zedoCN")
                        name.set("zedo")
                        email.set("zedoCN@outlook.com")
                    }
                }
            }
            artifact(tasks["sourcesJar"])
            artifact(tasks["javadocJar"])
        }
    }

    repositories {
        maven {
            url = uri("https://zedo.top:408/reposilite/releases")
            credentials {
                username = project.findProperty("repo.username") as String? ?: "username"
                password = project.findProperty("repo.password") as String? ?: "password"
            }
        }
    }
}
repositories {
    mavenCentral()
}

val ktorVersion: String by project

dependencies {
    implementation(kotlin("stdlib"))
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-json:$ktorVersion")
    implementation("io.ktor:ktor-client-serialization:$ktorVersion")
}

tasks.test {
    useJUnitPlatform()
}