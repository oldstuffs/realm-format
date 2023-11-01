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

include0(
  mapOf(
    ":format" to "realm-format-format",
    ":modifier" to null,
    ":modifier:core" to "realm-format-modifier-core",
    ":modifier:agent" to "realm-format-modifier-agent",
    ":bukkit" to null,
    ":bukkit:api" to "realm-format-bukkit-api",
    ":bukkit:nms" to null,
    ":bukkit:nms:common" to "realm-format-bukkit-nms-common",
    ":bukkit:nms:v1_19_R2" to "realm-format-bukkit-nms-v1_19_R2",
    ":bukkit:nms:v1_19_R3" to "realm-format-bukkit-nms-v1_19_R3",
    ":bukkit:plugin" to "realm-format-bukkit-plugin",
    ":modules" to null,
    ":modules:mongo" to "realm-format-modules-mongo",
    ":modules:redis" to "realm-format-modules-redis",
    ":modules:mariadb" to "realm-format-modules-mariadb",
  ),
)

fun include0(modules: Map<String, String?>) {
  modules.forEach { (module, projectName) ->
    include(module)
    if (projectName != null) {
      project(module).name = projectName
    }
  }
}
