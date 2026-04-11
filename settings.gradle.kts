rootProject.name = "cardboardbox"
include("api")
include("v1.20.6")
include("v1.21")
include("v1.21.3")
include("v1.21.4")
include("v1.21.5")
include("v1.21.6")
include("v1.21.9")
include("v1.21.11")
include("v26.1")

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}