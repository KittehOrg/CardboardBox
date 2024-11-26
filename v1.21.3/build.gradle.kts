plugins {
    id("java")
    id("io.papermc.paperweight.userdev") version "1.7.4"
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
    paperweight.paperDevBundle("1.21.3-R0.1-SNAPSHOT")
    compileOnly(project(":api"))
}

tasks.assemble {
    dependsOn(tasks.reobfJar)
}
