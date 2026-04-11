plugins {
    `meow-conventions-25`
    id("io.papermc.paperweight.userdev")
}

dependencies {
    paperweight.paperDevBundle("26.1.2.build.3-alpha")
    compileOnly(project(":api"))
}
