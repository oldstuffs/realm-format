dependencies {
  fun dep(dependencyId: String) = rootProject.property("dep.$dependencyId").toString()

  implementation(project(":paper:api"))

  implementation(dep("smol"))

  compileOnly(dep("paper"))

  smol(dep("configurate-yaml"))
  smol(dep("adventure-configurate")) { isTransitive = false }
  smol(dep("terminable"))
  smol(dep("task-common"))
  smol(dep("task-bukkit"))
  smol(dep("event-common"))
  smol(dep("event-paper"))
  smol(dep("cloud-core"))
  smol(dep("cloud-annotations"))
  smol(dep("cloud-paper"))
  smol(dep("cloud-brigadier"))
  smol(dep("cloud-tasks"))
  smol(dep("cloud-minecraft-extras")) { isTransitive = false }
  smol(dep("mariadb"))
  smol(dep("morphia")) { exclude("org.jetbrains", "annotations") }
}

tasks {
  processResources {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    from(project.the<SourceSetContainer>()["main"].resources.srcDirs) {
      expand("pluginVersion" to project.version)
      include("plugin.yml")
    }
  }
}
