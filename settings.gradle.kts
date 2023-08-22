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
    ":paper" to null,
    ":paper:api" to "realm-format-paper-api",
    ":paper:nms" to null,
    ":paper:nms:common" to "realm-format-paper-nms-common",
    ":paper:nms:v1_19_R3" to "realm-format-paper-nms-v1_19_R3",
    ":paper:plugin" to "realm-format-paper-plugin",
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
