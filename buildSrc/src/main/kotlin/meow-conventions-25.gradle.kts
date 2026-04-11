import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.maven
import org.gradle.kotlin.dsl.repositories

plugins {
    `java-library`
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
}

tasks {
    withType<JavaCompile>().configureEach {
        options.release = 25
    }
    withType<Javadoc>().configureEach {
        options.encoding = Charsets.UTF_8.name()
    }
}
