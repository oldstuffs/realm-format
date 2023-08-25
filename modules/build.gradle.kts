subprojects {
  dependencies {
    compileOnly(project(":bukkit:realm-format-bukkit-api"))

    compileOnly(rootProject.libs.pf4j)
  }

  tasks {
    jar {
      manifest {
        attributes(
          "Plugin-Id" to project.name,
          "Plugin-Version" to project.version,
          "Plugin-Provider" to "portlek",
        )
      }
    }
  }
}
