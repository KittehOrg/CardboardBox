plugins {
    id("java")
    id("com.gradleup.shadow") version "8.3.5"
    id("maven-publish")
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("com.destroystokyo.paper:paper-api:1.16.5-R0.1-SNAPSHOT")
    compileOnly("com.mojang:datafixerupper:1.0.20")
    implementation(project(":api"))
    implementation(project(":v1.20.6"))
    implementation(project(":v1.21"))
    implementation(project(":v1.21.3"))
}

java {
    targetCompatibility = JavaVersion.VERSION_17
    sourceCompatibility = JavaVersion.VERSION_17
}

tasks.jar {
    archiveClassifier = "original"
}

tasks.shadowJar {
    archiveClassifier = ""
}

publishing {
    publications {
        create<MavenPublication>("cardboardbox") {
            artifact(tasks.shadowJar)
            groupId = "dev.kitteh"
            artifactId = "cardboardbox"
            version = "2.0.2"
            pom {
                name = "CardboardBox"
                description = "A Bukkit-related data storage handler"
                licenses {
                    license {
                        name = "GNU General Public License (GPL) version 3"
                        url = "https://www.gnu.org/licenses/gpl-3.0.txt"
                    }
                }
                developers {
                    developer {
                        id = "mbaxter"
                        name = "Matt Baxter"
                        email = "matt@kitteh.org"
                        url = "https://www.kitteh.org/"
                        organization = "Kitteh"
                        organizationUrl = "https://www.kitteh.org"
                        roles = setOf("Lead Developer", "Cat Wrangler")
                    }
                }
                issueManagement {
                    system = "GitHub"
                    url = "https://github.com/KittehOrg/CardboardBox/issues"
                }
                scm {
                    connection = "scm:git:git://github.com/KittehOrg/CardboardBox.git"
                    developerConnection = "scm:git:git://github.com/KittehOrg/CardboardBox.git"
                    url = "git@github.com:KittehOrg/CardboardBox.git"
                }
                repositories {
                    maven {
                        name = "DependencyDownload"
                        val rel = "https://dependency.download/releases"
                        val snap = "https://dependency.download/snapshots"
                        url = uri(if (version.toString().endsWith("SNAPSHOT")) snap else rel)
                        credentials(PasswordCredentials::class)
                    }
                }
            }
        }
    }
}
