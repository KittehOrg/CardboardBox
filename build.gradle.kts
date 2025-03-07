plugins {
    `meow-conventions`
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.13" apply false
    id("com.gradleup.shadow") version "8.3.5"
    id("maven-publish")
}

group = "dev.kitteh"

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.17.1-R0.1-SNAPSHOT")
    compileOnly("com.mojang:datafixerupper:1.0.20")
    implementation(project(":api"))
    implementation(project(":v1.20.6", configuration = "reobf"))
    implementation(project(":v1.21", configuration = "reobf"))
    implementation(project(":v1.21.3", configuration = "reobf"))
    implementation(project(":v1.21.4", configuration = "reobf"))
}

tasks.assemble {
    dependsOn(tasks.shadowJar)
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
            version = "3.0.2"
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
