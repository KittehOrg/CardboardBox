plugins {
    `meow-conventions`
    id("io.papermc.paperweight.userdev")
}

dependencies {
    paperweight.paperDevBundle("1.21.5-no-moonrise-SNAPSHOT")
    compileOnly(project(":api"))
}
