plugins {
    id("java")
    id("io.papermc.paperweight.userdev") version "1.7.1"
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    paperweight.paperDevBundle("1.20.6-R0.1-SNAPSHOT")
    compileOnly(project(":api"))
}

tasks.assemble {
    dependsOn(tasks.reobfJar)
}
