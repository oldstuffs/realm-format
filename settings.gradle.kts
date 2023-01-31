pluginManagement {
  repositories {
    mavenCentral()
    gradlePluginPortal()
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://papermc.io/repo/repository/maven-public/")
    mavenLocal()
  }
}

rootProject.name = "realm-format"

include(
    "common",
    "modifier",
    "modifier:core",
    "modifier:agent",
    "paper:api",
    "paper:nms",
    "paper:nms:v1_18_R2",
    "paper:nms:v1_8_R3",
    "paper:plugin")
